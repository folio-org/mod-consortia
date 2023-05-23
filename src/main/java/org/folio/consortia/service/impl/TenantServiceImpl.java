package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.client.ConsortiaConfigurationClient;
import org.folio.consortia.client.UsersClient;
import org.folio.consortia.domain.dto.ConsortiaConfiguration;
import org.folio.consortia.domain.dto.PermissionUser;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.repository.TenantRepository;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.PermissionService;
import org.folio.consortia.service.PermissionUserService;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserTenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.folio.consortia.utils.HelperUtils.checkIdenticalOrThrow;
import static org.folio.consortia.utils.TenantContextUtils.prepareContextForTenant;

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
  private final PermissionUserService permissionUserService;
  private final PermissionService permissionService;
  private final UserTenantService userTenantService;
  private final FolioModuleMetadata folioModuleMetadata;

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
    User shadowAdminUser = userTenantService.prepareShadowUser(adminUserId, currentTenantContext.getTenantId());

    try (var context = new FolioExecutionContextSetter(prepareContextForTenant(tenantDto.getId(), folioModuleMetadata, folioExecutionContext))) {
      userTenantRepository.save(createUserTenantEntity(consortiumId, shadowAdminUser, tenantDto));
      createShadowAdminUserWithPermissions(shadowAdminUser);
      configurationClient.saveConfiguration(createConsortiaConfigurationBody(centralTenantId));
    }
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

  private void createShadowAdminUserWithPermissions(User user) {
    User userOptional = userTenantService.getUser(UUID.fromString(user.getId()));
    if (Objects.isNull(userOptional.getId())) {
      userOptional = createUser(user);
    }
    Optional<PermissionUser> permissionUserOptional = permissionUserService.getByUserId(userOptional.getId());
    if (permissionUserOptional.isPresent()) {
      permissionService.addPermissions(permissionUserOptional.get(), PERMISSIONS_FILE_PATH);
    } else {
      permissionService.createPermissionUser(user.getId(), PERMISSIONS_FILE_PATH);
    }
  }

  private User createUser(User user) {
    log.info("Creating user with id {}.", user.getId());
    usersClient.saveUser(user);
    return user;
  }

  private UserTenantEntity createUserTenantEntity(UUID consortiumId, User user, Tenant tenant) {
    UserTenantEntity userTenantEntity = new UserTenantEntity();
    TenantEntity tenantEntity = toEntity(consortiumId, tenant);

    userTenantEntity.setUserId(UUID.fromString(user.getId()));
    userTenantEntity.setId(UUID.randomUUID());
    userTenantEntity.setIsPrimary(Boolean.FALSE);
    userTenantEntity.setUsername(user.getUsername());
    userTenantEntity.setTenant(tenantEntity);
    return userTenantEntity;
  }
}
