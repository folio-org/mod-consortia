package org.folio.consortia.service.impl;

import java.util.UUID;

import org.folio.consortia.config.kafka.KafkaService;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserAffiliationService;
import org.folio.consortia.service.UserTenantService;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

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
    try {
      var userTenant = objectMapper.readValue(eventPayload, org.folio.consortia.domain.dto.UserEvent.class);
      // check if primary affiliation exists
      var consortiaTenant = tenantService.getByTenantId(userTenant.getTenantId());
      if (consortiaTenant == null) {
        log.warn("Tenant {} not exists in consortia", userTenant.getTenantId());
        return;
      }
      var consortiaAffiliation = userTenantService.getByUsernameAndTenantId(consortiaTenant.getConsortiumId(),
          userTenant.getUserDto().getUsername(),
          userTenant.getTenantId());

      var isPrimaryExists = consortiaAffiliation.getUserTenants()
        .stream()
        .anyMatch(UserTenant::getIsPrimary);
      if (isPrimaryExists) {
        log.warn("Primary affiliation already exists for the user: {}", userTenant.getUserDto().getUsername());
        return;
      }
      // create or update user tenant with primary affiliation
      var primaryAffiliationRecord = consortiaAffiliation.getUserTenants()
        .stream()
        .findFirst()
        .map(ut -> userTenantService.update(consortiaTenant.getConsortiumId(), ut.isPrimary(true)))
        .orElseGet(() -> {
          var ut = new UserTenant().userId(UUID.fromString(userTenant.getUserDto().getId()));
          return userTenantService.save(consortiaTenant.getConsortiumId(), ut);
        });
      kafkaService.send(KafkaService.Topic.CONSORTIUM_PRIMARY_AFFILIATION_CREATED, consortiaTenant.getConsortiumId().toString(), primaryAffiliationRecord);
      log.info("Primary affiliation has been set for the user: {}", userTenant.getUserDto().getId());
    } catch (Exception e) {
      log.error("Exception occurred while creating primary affiliation", e);
    }
  }

  @Override
  public void deletePrimaryUserAffiliation(String data) {
    // TODO : implement deletion with transactional outbox
    kafkaService.send(KafkaService.Topic.CONSORTIUM_PRIMARY_AFFILIATION_DELETED, "consortiaTenant.getConsortiumId().toString()", data);
  }
}
