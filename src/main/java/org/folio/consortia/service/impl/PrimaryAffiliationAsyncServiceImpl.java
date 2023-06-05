package org.folio.consortia.service.impl;

import static org.folio.consortia.utils.TenantContextUtils.prepareContextForTenant;

import java.util.UUID;
import java.util.stream.IntStream;

import org.folio.consortia.config.kafka.KafkaService;
import org.folio.consortia.domain.dto.PrimaryAffiliationEvent;
import org.folio.consortia.domain.dto.SyncPrimaryAffiliationBody;
import org.folio.consortia.domain.dto.SyncUser;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.PrimaryAffiliationAsyncService;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserTenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@AllArgsConstructor
public class PrimaryAffiliationAsyncServiceImpl implements PrimaryAffiliationAsyncService {

  private final UserTenantService userTenantService;
  private final TenantService tenantService;
  private final KafkaService kafkaService;
  private final UserTenantRepository userTenantRepository;
  private final ConversionService converter;
  private final FolioExecutionContext folioExecutionContext;
  private final FolioModuleMetadata folioModuleMetadata;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  @Async
  public void createPrimaryUserAffiliations(UUID consortiumId, SyncPrimaryAffiliationBody syncPrimaryAffiliationBody) {
    log.info("Start creating user primary affiliation for tenant {}", syncPrimaryAffiliationBody.getTenantId());
    var tenantId = syncPrimaryAffiliationBody.getTenantId();
    var userList = syncPrimaryAffiliationBody.getUsers();

    var centralTenantId = tenantService.getCentralTenantId();
    try (var context = new FolioExecutionContextSetter(prepareContextForTenant(centralTenantId, folioModuleMetadata, (FolioExecutionContext) folioExecutionContext.getInstance()))) {
      var tenantEntity = tenantService.getByTenantId(tenantId);
      IntStream.range(0, userList.size())
        .sequential()
        .forEach(idx -> {
          var user = userList.get(idx);
          log.info("Processing users: {} of {}", idx + 1, userList.size());
          var consortiaUserTenant = userTenantRepository.findByUserIdAndTenantId(UUID.fromString(user.getId()), tenantId)
            .orElse(null);
          if (consortiaUserTenant != null) {
            log.info("Primary affiliation already exists for tenant/user: {}/{}", tenantId, user.getUsername());
          } else {
            userTenantService.createPrimaryUserTenantAffiliation(consortiumId, tenantEntity, user.getId(), user.getUsername());
            sendCreatePrimaryAffiliationEvent(tenantEntity, user);
          }
        });
      log.info("Successfully created primary affiliations for tenant {}", tenantId);
    } catch (Exception e) {
      log.error("Failed to create primary affiliations for tenant {}", tenantId, e);
    }
  }

  @SneakyThrows
  private void sendCreatePrimaryAffiliationEvent(TenantEntity consortiaTenant, SyncUser user) {
    PrimaryAffiliationEvent affiliationEvent = createPrimaryAffiliationEvent(user, consortiaTenant.getId());
    String data = objectMapper.writeValueAsString(affiliationEvent);
    kafkaService.send(KafkaService.Topic.CONSORTIUM_PRIMARY_AFFILIATION_CREATED, consortiaTenant.getConsortiumId().toString(), data);
  }

  private PrimaryAffiliationEvent createPrimaryAffiliationEvent(SyncUser user, String tenantId) {
    PrimaryAffiliationEvent event = new PrimaryAffiliationEvent();
    event.setId(UUID.randomUUID());
    event.setUserId(UUID.fromString(user.getId()));
    event.setUsername(user.getUsername());
    event.setTenantId(tenantId);
    return event;
  }
}
