package org.folio.consortia.service.impl;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.folio.consortia.client.UsersClient;
import org.folio.consortia.config.kafka.KafkaService;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.domain.dto.UserEvent;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.UserAffiliationAsyncService;
import org.folio.consortia.service.UserTenantService;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@AllArgsConstructor
public class UserAffiliationAsyncServiceImpl implements UserAffiliationAsyncService {

  private final UserTenantService userTenantService;
  private final KafkaService kafkaService;
  private final UsersClient usersClient;
  private final UserTenantRepository userTenantRepository;

  public CompletableFuture<Void> createPrimaryUserAffiliationsAsync(UUID consortiumId, TenantEntity consortiaTenant,
    Tenant tenantDto, UUID contextUserId) {
    return CompletableFuture.runAsync(() -> {
        log.info("Start creating user primary affiliation for tenant {}", tenantDto.getId());
        var users = usersClient.getUserCollection(StringUtils.EMPTY, 0, Integer.MAX_VALUE);
        log.info("{} tenant users found", users.getUsers().size());
        IntStream.range(0, users.getUsers().size())
          .forEach(idx -> {
            var user = users.getUsers().get(idx);
            log.info("Processing users: {} of {}", idx + 1, users.getUsers().size());
            var consortiaUserTenant = userTenantRepository.findByUserIdAndTenantId(UUID.fromString(user.getId()), tenantDto.getId())
              .orElse(null);
            if (consortiaUserTenant != null && consortiaUserTenant.getIsPrimary()) {
              log.info("Primary affiliation already exists for tenant/user: {}/{}", tenantDto.getId(), user.getUsername());
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
}
