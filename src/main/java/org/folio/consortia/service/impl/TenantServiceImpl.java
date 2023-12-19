package org.folio.consortia.service.impl;

import static org.folio.consortia.utils.HelperUtils.checkIdenticalOrThrow;
import static org.folio.consortia.utils.TenantContextUtils.prepareContextForTenant;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.folio.consortia.client.ConsortiaConfigurationClient;
import org.folio.consortia.client.SyncPrimaryAffiliationClient;
import org.folio.consortia.client.UserTenantsClient;
import org.folio.consortia.config.FolioExecutionContextHelper;
import org.folio.consortia.domain.dto.ConsortiaConfiguration;
import org.folio.consortia.domain.dto.PermissionUser;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.domain.dto.TenantDetails;
import org.folio.consortia.domain.dto.TenantDetails.SetupStatusEnum;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.entity.TenantDetailsEntity;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.repository.TenantDetailsRepository;
import org.folio.consortia.repository.TenantRepository;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.CleanupService;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.LockService;
import org.folio.consortia.service.PermissionUserService;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

  private static final String SHADOW_ADMIN_PERMISSION_FILE_PATH = "permissions/admin-user-permissions.csv";
  private static final String SHADOW_SYSTEM_USER_PERMISSION_FILE_PATH = "permissions/system-user-permissions.csv";
  private static final String TENANTS_IDS_NOT_MATCHED_ERROR_MSG = "Request body tenantId and path param tenantId should be identical";

  private static final String DUMMY_USERNAME = "dummy_user";
  @Value("${folio.system-user.username}")
  private String systemUserUsername;

  private final TenantRepository tenantRepository;
  private final TenantDetailsRepository tenantDetailsRepository;
  private final UserTenantRepository userTenantRepository;
  private final ConversionService converter;
  private final ConsortiumService consortiumService;
  private final FolioExecutionContext folioExecutionContext;
  private final ConsortiaConfigurationClient configurationClient;
  private final PermissionUserService permissionUserService;
  private final UserService userService;
  private final FolioExecutionContextHelper contextHelper;
  private final UserTenantsClient userTenantsClient;
  private final SyncPrimaryAffiliationClient syncPrimaryAffiliationClient;
  private final CleanupService cleanupService;
  private final LockService lockService;

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
  public TenantCollection getAll(UUID consortiumId) {
    TenantCollection result = new TenantCollection();
    List<Tenant> list = tenantRepository.findByConsortiumId(consortiumId)
      .stream().map(o -> converter.convert(o, Tenant.class)).toList();
    result.setTenants(list);
    result.setTotalRecords(list.size());
    return result;
  }

  @Override
  public TenantDetails getTenantDetailsById(UUID consortiumId, String tenantId) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    var tenantDetailsEntity = tenantDetailsRepository.findById(tenantId).orElseThrow(() ->
      new ResourceNotFoundException("tenantId", tenantId));
    return converter.convert(tenantDetailsEntity, TenantDetails.class);
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
    log.info("save:: Trying to save a tenant by consortiumId '{}', tenant object with id '{}' and isCentral={}", consortiumId,
        tenantDto.getId(), tenantDto.getIsCentral());

    // validation part
    checkTenantNotExistsAndConsortiumExistsOrThrow(consortiumId, tenantDto.getId());
    checkCodeAndNameUniqueness(tenantDto);
    if (tenantDto.getIsCentral()) {
      checkCentralTenantExistsOrThrow();
    }

    // save tenant to db
    lockService.lockTenantSetupWithinTransaction();
    Tenant savedTenant = saveTenant(consortiumId, tenantDto, SetupStatusEnum.IN_PROGRESS);

    // save admin user tenant association for non-central tenant
    String centralTenantId;
    User shadowAdminUser = null;
    User shadowSystemUser = null;
    if (tenantDto.getIsCentral()) {
      centralTenantId = tenantDto.getId();
    } else {
      checkAdminUserIdPresentOrThrow(adminUserId);
      centralTenantId = getCentralTenantId();
      shadowAdminUser = userService.prepareShadowUser(adminUserId, folioExecutionContext.getTenantId());
      userTenantRepository.save(createUserTenantEntity(consortiumId, shadowAdminUser, tenantDto));
      // creating shadow user of consortia system user of central tenant with same permissions.
      var centralSystemUser = userService.getByUsername(systemUserUsername)
        .orElseThrow(() ->  new ResourceNotFoundException("systemUserUsername", systemUserUsername));
      shadowSystemUser = userService.prepareShadowUser(UUID.fromString(centralSystemUser.getId()), folioExecutionContext.getTenantId());
      userTenantRepository.save(createUserTenantEntity(consortiumId, shadowSystemUser, tenantDto));
    }

    // switch to context of the desired tenant and apply all necessary setup
    try (var ignored = new FolioExecutionContextSetter(contextHelper.getSystemUserFolioExecutionContext(tenantDto.getId()))) {
      configurationClient.saveConfiguration(createConsortiaConfigurationBody(centralTenantId));
      if (!tenantDto.getIsCentral()) {
        createUserTenantWithDummyUser(tenantDto.getId(), centralTenantId, consortiumId);
        createShadowUserWithPermissions(shadowAdminUser, SHADOW_ADMIN_PERMISSION_FILE_PATH); //NOSONAR
        log.info("save:: shadow admin user '{}' with permissions was created in tenant '{}'", shadowAdminUser.getId(), tenantDto.getId());
        createShadowUserWithPermissions(shadowSystemUser, SHADOW_SYSTEM_USER_PERMISSION_FILE_PATH);
        log.info("save:: shadow system user '{}' with permissions was created in tenant '{}'", shadowSystemUser.getId(), tenantDto.getId());
      }
      syncPrimaryAffiliationClient.syncPrimaryAffiliations(consortiumId.toString(), tenantDto.getId(), centralTenantId);
    }
    log.info("save:: saved consortia configuration with centralTenantId={} by tenantId={} context", centralTenantId, tenantDto.getId());
    return savedTenant;
  }

  @Override
  @Transactional
  public Tenant update(UUID consortiumId, String tenantId, Tenant tenantDto) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    checkTenantExistsOrThrow(tenantId);
    checkIdenticalOrThrow(tenantId, tenantDto.getId(), TENANTS_IDS_NOT_MATCHED_ERROR_MSG);
    return updateTenant(consortiumId, tenantDto);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateTenantSetupStatus(String tenantId, String centralTenantId, SetupStatusEnum setupStatus) {
    try (var ctx = new FolioExecutionContextSetter(prepareContextForTenant(centralTenantId,
      folioExecutionContext.getFolioModuleMetadata(), folioExecutionContext))) {
      tenantDetailsRepository.setSetupStatusByTenantId(setupStatus, tenantId);
      log.info("updateTenantSetupStatus:: tenant id={} status updated to {}", tenantId, setupStatus);
    }
  }

  @Override
  @Transactional
  public void delete(UUID consortiumId, String tenantId) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    var tenant = tenantRepository.findById(tenantId);

    if (tenant.isEmpty()) {
      throw new ResourceNotFoundException("id", tenantId);
    }
    if (tenant.get().getIsCentral()) {
      throw new IllegalArgumentException(String.format("central tenant %s cannot be deleted", tenantId));
    }

    var softDeletedTenant = tenant.get();
    softDeletedTenant.setIsDeleted(true);
    // clean publish coordinator tables first, because after tenant removal it will be ignored by cleanup service
    cleanupService.clearPublicationTables();
    tenantRepository.save(softDeletedTenant);

//    try (var ignored = new FolioExecutionContextSetter(contextHelper.getSystemUserFolioExecutionContext(tenantId))) {
//      userTenantsClient.deleteUserTenants();
//    }
  }

  private Tenant saveTenant(UUID consortiumId, Tenant tenantDto, SetupStatusEnum setupStatus) {
    log.debug("saveTenant:: Trying to save tenant with consoritumId={} and tenant with id={}, setupStatus={}",
      consortiumId, tenantDto, setupStatus);
    TenantDetailsEntity entity = toTenantDetailsEntity(consortiumId, tenantDto, setupStatus);
    TenantDetailsEntity savedTenant = tenantDetailsRepository.save(entity);
    log.info("saveTenant: Tenant '{}' successfully saved, setupStatus={}", savedTenant.getId(), savedTenant.getSetupStatus());
    return converter.convert(savedTenant, Tenant.class);
  }

  private Tenant updateTenant(UUID consortiumId, Tenant tenantDto) {
    log.debug("updateTenant:: Trying to update tenant with consoritumId={} and tenant with id={}", consortiumId, tenantDto);
    TenantEntity entity = toTenantEntity(consortiumId, tenantDto);
    TenantEntity updatedTenant = tenantRepository.save(entity);
    log.info("updateTenant:: Tenant '{}' successfully updated", updatedTenant.getId());
    return converter.convert(updatedTenant, Tenant.class);
  }

  /*
    Dummy user will be used to support cross-tenant requests checking in mod-authtoken,
    if user-tenant table contains some record in institutional tenant - it means mod-consortia enabled for
    this tenant and will allow cross-tenant request.

    @param tenantId tenant id
    @param centralTenantId central tenant id
    @param consortiumId consortium id
  */
  private void createUserTenantWithDummyUser(String tenantId, String centralTenantId, UUID consortiumId) {
    UserTenant userTenant = new UserTenant();
    userTenant.setId(UUID.randomUUID());
    userTenant.setTenantId(tenantId);
    userTenant.setUserId(UUID.randomUUID());
    userTenant.setUsername(DUMMY_USERNAME);
    userTenant.setCentralTenantId(centralTenantId);
    userTenant.setConsortiumId(consortiumId);

    log.info("Creating userTenant with dummy user with id {}.", userTenant.getId());
    userTenantsClient.postUserTenant(userTenant);
  }

  private void checkTenantNotExistsAndConsortiumExistsOrThrow(UUID consortiumId, String tenantId) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    if (tenantRepository.existsById(tenantId)) {
      throw new ResourceAlreadyExistException("id", tenantId);
    }
  }

  private void checkCodeAndNameUniqueness(Tenant tenant) {
    if (tenantRepository.existsByName(tenant.getName())) {
      throw new ResourceAlreadyExistException("name", tenant.getName());
    }
    if (tenantRepository.existsByCode(tenant.getCode())) {
      throw new ResourceAlreadyExistException("code", tenant.getCode());
    }
  }

  @Override
  public void checkTenantExistsOrThrow(String tenantId) {
    if (!tenantRepository.existsById(tenantId)) {
      throw new ResourceNotFoundException("id", tenantId);
    }
  }

  @Override
  public void checkTenantsAndConsortiumExistsOrThrow(UUID consortiumId, List<String> tenantIds) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    var tenantEntities = tenantRepository.findAllById(tenantIds);

    if (tenantEntities.size() != tenantIds.size()) {
      var foundTenantIds = tenantEntities.stream()
        .map(TenantEntity::getId)
        .toList();
      String absentTenants = String.join(", ", CollectionUtils.subtract(tenantIds, foundTenantIds));
      log.warn("Tenants with ids {} not found", absentTenants);

      throw new ResourceNotFoundException("ids", absentTenants);
    }
  }

  private void checkCentralTenantExistsOrThrow() {
    if (tenantRepository.existsByIsCentralTrue()) {
      throw new ResourceAlreadyExistException("isCentral", "true");
    }
  }

  private void checkAdminUserIdPresentOrThrow(UUID adminUserId) {
    if (Objects.isNull(adminUserId)) {
      log.warn("checkAdminUserIdPresentOrThrow:: adminUserId is not present");
      throw new IllegalArgumentException("Required request parameter 'adminUserId' for method parameter type UUID is not present");
    }
  }

  private TenantEntity toTenantEntity(UUID consortiumId, Tenant tenantDto) {
    TenantEntity entity = new TenantEntity();
    entity.setId(tenantDto.getId());
    entity.setName(tenantDto.getName());
    entity.setCode(tenantDto.getCode());
    entity.setIsCentral(tenantDto.getIsCentral());
    entity.setConsortiumId(consortiumId);
    entity.setIsDeleted(tenantDto.getIsDeleted());
    return entity;
  }

  private TenantDetailsEntity toTenantDetailsEntity(UUID consortiumId, Tenant tenantDto, SetupStatusEnum setupStatus) {
    TenantDetailsEntity entity = new TenantDetailsEntity();
    entity.setId(tenantDto.getId());
    entity.setName(tenantDto.getName());
    entity.setCode(tenantDto.getCode());
    entity.setIsCentral(tenantDto.getIsCentral());
    entity.setConsortiumId(consortiumId);
    entity.setSetupStatus(setupStatus);
    entity.setIsDeleted(tenantDto.getIsDeleted());
    return entity;
  }

  private ConsortiaConfiguration createConsortiaConfigurationBody(String tenantId) {
    ConsortiaConfiguration configuration = new ConsortiaConfiguration();
    configuration.setCentralTenantId(tenantId);
    return configuration;
  }

  private void createShadowUserWithPermissions(User user, String permissionFilePath) {
    User userOptional = userService.getById(UUID.fromString(user.getId()));
    if (Objects.isNull(userOptional.getId())) {
      userOptional = userService.createUser(user);
    }
    Optional<PermissionUser> permissionUserOptional = permissionUserService.getByUserId(userOptional.getId());
    if (permissionUserOptional.isPresent()) {
      permissionUserService.addPermissions(permissionUserOptional.get(), permissionFilePath);
    } else {
      permissionUserService.createWithPermissionsFromFile(user.getId(), permissionFilePath);
    }
  }

  private UserTenantEntity createUserTenantEntity(UUID consortiumId, User user, Tenant tenant) {
    UserTenantEntity userTenantEntity = new UserTenantEntity();
    TenantEntity tenantEntity = toTenantEntity(consortiumId, tenant);

    userTenantEntity.setUserId(UUID.fromString(user.getId()));
    userTenantEntity.setId(UUID.randomUUID());
    userTenantEntity.setIsPrimary(Boolean.FALSE);
    userTenantEntity.setUsername(user.getUsername());
    userTenantEntity.setTenant(tenantEntity);
    return userTenantEntity;
  }
}
