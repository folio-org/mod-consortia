package org.folio.consortia.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.folio.consortia.config.kafka.KafkaService;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserAffiliationService;
import org.folio.consortia.service.UserTenantService;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;

@Service
@Log4j2
@AllArgsConstructor
public class UserAffiliationServiceImpl implements UserAffiliationService {

  private final UserTenantService userTenantService;
  private final TenantService tenantService;
  private final KafkaService kafkaService;
  private final ObjectMapper objectMapper;

  @Override
  @SneakyThrows
  public void createPrimaryUserAffiliation(String eventPayload) {
    var userTenant = new UserTenant().tenantId("diku").username(UUID.randomUUID().toString());
    //var userTenant = objectMapper.readValue(eventPayload, UserTenant.class);
    // check if primary affiliation exists
    var consortiaTenant = tenantService.getByTenantId(userTenant.getTenantId());
    if (consortiaTenant == null) {
      log.warn("Tenant {} not exists in consortia", userTenant.getTenantId());
      return;
    }
    var consortiaAffiliation = userTenantService.getByUsernameAndTenantId(consortiaTenant.getConsortiumId(), userTenant.getUsername(), userTenant.getTenantId());
    var isPrimaryExists = consortiaAffiliation.getUserTenants().stream()
      .anyMatch(UserTenant::getIsPrimary);

    if (isPrimaryExists) {
      log.warn("Primary affiliation already exists for the user: {}", userTenant.getUsername());
      return;
    }
    // create or update user tenant with primary affiliation
    var primaryAffiliationRecord = consortiaAffiliation.getUserTenants()
      .stream()
      .findFirst()
      .map(ut -> userTenantService.update(consortiaTenant.getConsortiumId(), ut.isPrimary(true)))
      .orElseGet(() -> userTenantService.save(consortiaTenant.getConsortiumId(), userTenant.isPrimary(true)));
    kafkaService.send(KafkaService.Topic.CONSORTIUM_PRIMARY_AFFILIATION_CREATED, consortiaTenant.getConsortiumId().toString(), primaryAffiliationRecord);
    log.info("Primary affiliation has been set for the user: {}", userTenant.getUserId());
  }

  @Override
  public void deletePrimaryUserAffiliation(String data) {
    // TODO : implement deletion with transactional outbox
    kafkaService.send(KafkaService.Topic.CONSORTIUM_PRIMARY_AFFILIATION_DELETED, "consortiaTenant.getConsortiumId().toString()", data);
  }
}
