package org.folio.consortia.service.impl;

import static org.folio.consortia.utils.HelperUtils.checkIdenticalOrThrow;
import static org.folio.consortia.utils.TenantContextUtils.createFolioExecutionContextForTenant;
import static org.folio.consortia.utils.TenantContextUtils.runInFolioContext;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.folio.consortia.client.ConsortiaConfigurationClient;
import org.folio.consortia.client.UsersClient;
import org.folio.consortia.config.kafka.KafkaService;
import org.folio.consortia.domain.dto.ConsortiaConfiguration;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.domain.dto.UserEvent;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.repository.TenantRepository;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserTenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

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
  private final UserTenantService userTenantService;
  private final KafkaService kafkaService;

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
  public Tenant save(UUID consortiumId, Tenant tenantDto, boolean forceCreatePrimaryAff) {
    log.debug("save:: Trying to save a tenant by consortiumId '{}', tenant object with id '{}' and isCentral={}", consortiumId, tenantDto.getId(), tenantDto.getIsCentral());
    FolioExecutionContext currentTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();
    String centralTenantId;
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);

    if (tenantDto.getIsCentral()) {
      checkCentralTenantExistsOrThrow();
      centralTenantId = tenantDto.getId();
    } else {
      centralTenantId = getCentralTenantId();
    }

    TenantEntity tenantEntity;
    if (forceCreatePrimaryAff) {
      tenantEntity = getOrCreateTenantEntity(consortiumId, tenantDto);
    }
    else {
      checkTenantNotExistsOrThrow(tenantDto.getId());
      tenantEntity = saveTenantEntity(consortiumId, tenantDto);
    }
    var savedTenant = converter.convert(tenantEntity, Tenant.class);

    runInFolioContext(createFolioExecutionContextForTenant(tenantDto.getId(), currentTenantContext, folioMetadata),
        () -> {
          configurationClient.saveConfiguration(createConsortiaConfigurationBody(centralTenantId));
          createPrimaryUserAffiliationsAsync(consortiumId, tenantEntity, tenantDto, currentTenantContext.getUserId());
        });
    log.info("save:: saved consortia configuration with centralTenantId={} by tenantId={} context", centralTenantId, tenantDto.getId());

    return savedTenant;
  }

  private TenantEntity getOrCreateTenantEntity(UUID consortiumId, Tenant tenantDto) {
    if (tenantRepository.existsById(tenantDto.getId())) {
      return getByTenantId(tenantDto.getId());
    } else {
      return saveTenantEntity(consortiumId, tenantDto);
    }
  }

  public CompletableFuture<Void> createPrimaryUserAffiliationsAsync(UUID consortiumId, TenantEntity consortiaTenant, Tenant tenantDto,
    UUID contextUserId) {
    return CompletableFuture
      .runAsync(() -> {
        log.info("Start creating user primary affiliation for tenant {}", tenantDto.getId());
        var users = usersClient.getUserCollection(StringUtils.EMPTY, 0, Integer.MAX_VALUE);
        users.getUsers()
          .forEach(user -> {
            var consortiaUserTenant = userTenantRepository.findByUserIdAndTenantId(UUID.fromString(user.getId()), tenantDto.getId())
              .orElse(null);
            if (consortiaUserTenant != null && consortiaUserTenant.getIsPrimary()) {
              log.debug("Primary affiliation already exists for tenant/user: {}/{}", tenantDto.getId(), user.getUsername());
            } else {
              userTenantService.createPrimaryUserTenantAffiliation(consortiumId, consortiaTenant, user.getId(), user.getUsername());
              sendCreatePrimaryAffiliationEvent(consortiaTenant, tenantDto, contextUserId, user);
            }
          });
      })
      .thenAccept(v -> log.info("Successfully created primary affiliations for tenant {}", tenantDto.getId()))
      .exceptionally(t -> {
        log.error("Failed to create primary affiliations for new tenant", t);
        return null;
      });
  }

  private void sendCreatePrimaryAffiliationEvent(TenantEntity consortiaTenant, Tenant tenantDto, UUID contextUserId, User user) {
    var ue = new UserEvent().tenantId(tenantDto.getId())
      .userDto(user)
      .action(UserEvent.ActionEnum.CREATE)
      .actionDate(new Date())
      .eventDate(new Date())
      .performedBy(contextUserId);

    kafkaService.send(KafkaService.Topic.CONSORTIUM_PRIMARY_AFFILIATION_CREATED, consortiaTenant.getConsortiumId().toString(), ue.toString());
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
    TenantEntity savedTenant = saveTenantEntity(consortiumId, tenantDto);
    return converter.convert(savedTenant, Tenant.class);
  }

  private TenantEntity saveTenantEntity(UUID consortiumId, Tenant tenantDto) {
    log.debug("saveTenant:: Trying to save tenant with consoritumId={} and tenant with id={}", consortiumId, tenantDto);
    TenantEntity entity = toEntity(consortiumId, tenantDto);
    TenantEntity savedTenant = tenantRepository.save(entity);
    log.info("saveTenant: Tenant '{}' successfully saved", savedTenant.getId());
    return savedTenant;
  }

  private void checkTenantNotExistsAndConsortiumExistsOrThrow(UUID consortiumId, String tenantId) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    if (tenantRepository.existsById(tenantId)) {
      throw new ResourceAlreadyExistException("id", tenantId);
    }
  }

  private void checkTenantNotExistsOrThrow(String tenantId) {
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
}
