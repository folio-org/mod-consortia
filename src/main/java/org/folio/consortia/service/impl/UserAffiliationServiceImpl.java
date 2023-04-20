package org.folio.consortia.service.impl;

import org.folio.consortia.domain.dto.UserEvent;
import org.folio.consortia.config.kafka.KafkaService;
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
      var userEvent = objectMapper.readValue(eventPayload, UserEvent.class);
      // check if tenant is part of consortia
      var consortiaTenant = tenantService.getByTenantId(userEvent.getTenantId());
      if (consortiaTenant == null) {
        log.warn("Tenant {} not exists in consortia", userEvent.getTenantId());
        return;
      }
      // check if tenant is part of consortia
      var consortiaUserTenant = userTenantService.getByUsernameAndTenantIdOrNull(consortiaTenant.getConsortiumId(),
        userEvent.getUserDto().getUsername(),
        userEvent.getTenantId());
      if (consortiaUserTenant != null && consortiaUserTenant.getIsPrimary()) {
        log.warn("Primary affiliation already exists for tenant/user: {}/{}", userEvent.getTenantId(), userEvent.getUserDto().getUsername());
        return;
      } else {
        userTenantService.createPrimaryUserTenantAffiliation(consortiaTenant.getConsortiumId(), consortiaTenant, userEvent);
      }

      kafkaService.send(KafkaService.Topic.CONSORTIUM_PRIMARY_AFFILIATION_CREATED, consortiaTenant.getConsortiumId().toString(), eventPayload);
      log.info("Primary affiliation has been set for the user: {}", userEvent.getUserDto().getId());
    } catch (Exception e) {
      log.error("Exception occurred while creating primary affiliation", e);
    }
  }

  @Override
  public void deletePrimaryUserAffiliation(String data) {
    // TODO : implement deletion with transactional outbox
    try {
      var userEvent = objectMapper.readValue(data, UserEvent.class);
      // checking whether tenant part of consortia
      var consortiaTenant = tenantService.getByTenantId(userEvent.getTenantId());
      if (consortiaTenant == null) {
        log.warn("Tenant {} not exists in consortia", userEvent.getTenantId());
        return;
      }
      var consortiaUserTenant = userTenantService.getByUsernameAndTenantIdOrNull(consortiaTenant.getConsortiumId(), userEvent.getUserDto().getUsername(),
        userEvent.getTenantId());
      if (consortiaUserTenant != null && consortiaUserTenant.getIsPrimary()) {
        log.warn("Primary affiliation already exists for tenant/user: {}/{}", userEvent.getTenantId(), userEvent.getUserDto().getUsername());
        return;
      } else {

      }


      kafkaService.send(KafkaService.Topic.CONSORTIUM_PRIMARY_AFFILIATION_DELETED, "consortiaTenant.getConsortiumId().toString()", data);
    } catch (Exception e) {
      log.error("Exception occurred while deleting primary affiliation", e);
    }
  }
}
