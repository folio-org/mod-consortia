package org.folio.consortia.service.impl;

import com.google.common.io.Resources;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.folio.consortia.client.ConsortiaConfigurationClient;
import org.folio.consortia.client.PermissionsClient;
import org.folio.consortia.client.UsersClient;
import org.folio.consortia.domain.dto.ConsortiaConfiguration;
import org.folio.consortia.domain.dto.PermissionUser;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.repository.TenantRepository;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.TenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.folio.consortia.utils.HelperUtils.checkIdenticalOrThrow;
import static org.folio.consortia.utils.TenantContextUtils.createFolioExecutionContextForTenant;
import static org.folio.consortia.utils.TenantContextUtils.runInFolioContext;

@Service
@Log4j2
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

  private static final String PERMISSIONS_FILE_PATH = "permissions/admin-user-permissions.csv";
  private static final String TENANTS_IDS_NOT_MATCHED_ERROR_MSG = "Request body tenantId and path param tenantId should be identical";
  private static final String TENANT_HAS_ACTIVE_USER_ASSOCIATIONS_ERROR_MSG = "Cannot delete tenant with ID {tenantId} because it has an association with a user. " +
    "Please remove the user association before attempting to delete the tenant.";
  private final TenantRepository tenantRepository;
  private final UserTenantRepository userTenantRepository;
  private final ConversionService converter;
  private final ConsortiumService consortiumService;
  private final FolioExecutionContext folioExecutionContext;
  private final FolioModuleMetadata folioMetadata;
  private final ConsortiaConfigurationClient configurationClient;
  private final UsersClient usersClient;
  private final PermissionsClient permissionsClient;
  private final UserTenantServiceImpl userTenantService;

  @Override
  public TenantCollection get(UUID consortiumId, Integer offset, Integer limit) {
    TenantCollection result = new TenantCollection();
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    Page<TenantEntity> page = tenantRepository.findByConsortiumId(consortiumId, PageRequest.of(offset, limit));
    result.setTenants(page.map(o -> converter.convert(o, Tenant.class)).getContent());
    result.setTotalRecords((int) page.getTotalElements());
    return result;
  }

  @Override
  public String getCentralTenantId() {
    TenantEntity tenant = tenantRepository.findCentralTenant()
      .orElseThrow(() -> new ResourceNotFoundException("A central tenant is not found. The central tenant must be created"));
    return tenant.getId();
  }

  @Override
  public TenantEntity getByTenantId(String tenantId) {
    return tenantRepository.findById(tenantId)
      .orElse(null);
  }

  @Override
  @Transactional
  public Tenant save(UUID consortiumId, UUID adminUserId, Tenant tenantDto) {
    log.debug("save:: Trying to save a tenant by consortiumId '{}', tenant object with id '{}' and isCentral={}", consortiumId, tenantDto.getId(), tenantDto.getIsCentral());
    FolioExecutionContext currentTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();
    String centralTenantId;

    if (tenantDto.getIsCentral()) {
      checkCentralTenantExistsOrThrow();
      centralTenantId = tenantDto.getId();
    } else {
      centralTenantId = getCentralTenantId();
    }

    checkTenantNotExistsAndConsortiumExistsOrThrow(consortiumId, tenantDto.getId());
    Tenant savedTenant = saveTenant(consortiumId, tenantDto);

    runInFolioContext(createFolioExecutionContextForTenant(tenantDto.getId(), currentTenantContext, folioMetadata),
      () -> {
        createShadowAdminUserWithPermissions(adminUserId);
        configurationClient.saveConfiguration(createConsortiaConfigurationBody(centralTenantId));
      }
    );
    log.info("save:: saved consortia configuration with centralTenantId={} by tenantId={} context", centralTenantId, tenantDto.getId());

    return savedTenant;
  }

  @Override
  public Tenant update(UUID consortiumId, String tenantId, Tenant tenantDto) {
    checkTenantAndConsortiumExistsOrThrow(consortiumId, tenantId);
    checkIdenticalOrThrow(tenantId, tenantDto.getId(), TENANTS_IDS_NOT_MATCHED_ERROR_MSG);
    return saveTenant(consortiumId, tenantDto);
  }

  @Override
  public void delete(UUID consortiumId, String tenantId) {
    checkTenantAndConsortiumExistsOrThrow(consortiumId, tenantId);
    if (userTenantRepository.existsByTenantId(tenantId)) {
      throw new IllegalArgumentException(TENANT_HAS_ACTIVE_USER_ASSOCIATIONS_ERROR_MSG);
    }
    tenantRepository.deleteById(tenantId);
  }

  private Tenant saveTenant(UUID consortiumId, Tenant tenantDto) {
    log.debug("saveTenant:: Trying to save tenant with consoritumId={} and tenant with id={}", consortiumId, tenantDto);
    TenantEntity entity = toEntity(consortiumId, tenantDto);
    TenantEntity savedTenant = tenantRepository.save(entity);
    log.info("saveTenant: Tenant '{}' successfully saved", savedTenant.getId());
    return converter.convert(savedTenant, Tenant.class);
  }

  private void checkTenantNotExistsAndConsortiumExistsOrThrow(UUID consortiumId, String tenantId) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    if (tenantRepository.existsById(tenantId)) {
      throw new ResourceAlreadyExistException("id", tenantId);
    }
  }

  private void checkTenantAndConsortiumExistsOrThrow(UUID consortiumId, String tenantId) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    if (!tenantRepository.existsById(tenantId)) {
      throw new ResourceNotFoundException("id", tenantId);
    }
  }

  private void checkCentralTenantExistsOrThrow() {
    if (tenantRepository.existsByIsCentralTrue()) {
      throw new ResourceAlreadyExistException("isCentral", "true");
    }
  }

  private TenantEntity toEntity(UUID consortiumId, Tenant tenantDto) {
    TenantEntity entity = new TenantEntity();
    entity.setId(tenantDto.getId());
    entity.setName(tenantDto.getName());
    entity.setCode(tenantDto.getCode());
    entity.setIsCentral(tenantDto.getIsCentral());
    entity.setConsortiumId(consortiumId);
    return entity;
  }

  private ConsortiaConfiguration createConsortiaConfigurationBody(String tenantId) {
    ConsortiaConfiguration configuration = new ConsortiaConfiguration();
    configuration.setCentralTenantId(tenantId);
    return configuration;
  }

  private void createShadowAdminUserWithPermissions(UUID userId) {
    User userOptional = userTenantService.getUser(userId);
    if (Objects.isNull(userOptional.getId())) {
      userOptional = createUser(userId);
    }
    Optional<PermissionUser> permissionUserOptional = permissionsClient.get("userId==" + userOptional.getId())
      .getPermissionUsers()
      .stream()
      .findFirst();
    if (permissionUserOptional.isEmpty()) {
      createPermissionUser(userOptional.getId());
    }
  }

  private PermissionUser createPermissionUser(String userId) {
    List<String> perms = readPermissionsFromResource(PERMISSIONS_FILE_PATH);

    if (CollectionUtils.isEmpty(perms)) {
      throw new IllegalStateException("No user permissions found in " + PERMISSIONS_FILE_PATH);
    }

    var permissionUser = PermissionUser.of(UUID.randomUUID().toString(), userId, perms);
    log.info("Creating {}.", permissionUser);
    return permissionsClient.create(permissionUser);
  }

  private List<String> readPermissionsFromResource(String permissionsFilePath) {
    List<String> result = new ArrayList<>();
    var url = Resources.getResource(permissionsFilePath);

    try {
      result = Resources.readLines(url, StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error(String.format("Can't read user permissions from %s.", permissionsFilePath), e);
    }

    return result;
  }

  private User createUser(UUID userId) {
    var result = createUserObject(userId);
    log.info("Creating {}.", result);
    usersClient.saveUser(result);
    return result;
  }

  private User createUserObject(UUID userId) {
    final var result = new User();
    result.setId(userId.toString());
    result.setUsername("Admin");
    result.setActive(true);
    return result;
  }
}
