package org.folio.consortia.service.impl;

import static org.folio.spring.scope.FolioExecutionScopeExecutionContextManager.getRunnableWithCurrentFolioContext;
import static org.folio.consortia.utils.TenantContextUtils.prepareContextForTenant;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.folio.consortia.config.kafka.KafkaService;
import org.folio.consortia.domain.dto.PrimaryAffiliationEvent;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.UserAffiliationAsyncService;
import org.folio.consortia.service.UserService;
import org.folio.consortia.service.UserTenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@AllArgsConstructor
public class UserAffiliationAsyncServiceImpl implements UserAffiliationAsyncService {

  private final FolioExecutionContext folioExecutionContext;
  private final FolioModuleMetadata folioModuleMetadata;
  private final UserTenantService userTenantService;
  private final KafkaService kafkaService;
  private final UserService userService;
  private final UserTenantRepository userTenantRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Executor asyncTaskExecutor;


  public void createPrimaryUserAffiliationsAsync(UUID consortiumId, String centralTenantId, TenantEntity consortiaTenant) {
    asyncTaskExecutor.execute(getRunnableWithCurrentFolioContext(() -> {
      FolioExecutionContext currentTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();

      try {
        log.info("Start creating user primary affiliation for tenant {}", consortiaTenant.getId());
        var users = userService.getUsersByQuery("cql.allRecords=1", 0, Integer.MAX_VALUE);
        log.info("{} tenant users found", users.size());

        try (var context = new FolioExecutionContextSetter(prepareContextForTenant(centralTenantId, folioModuleMetadata, currentTenantContext))) {
          IntStream.range(0, users.size())
            .forEach(idx -> {
              log.info("Processing users: {} of {}", idx + 1, users.size());

              var user = users.get(idx);
              var userTenantPage = userTenantRepository.findByUserId(UUID.fromString(user.getId()), PageRequest.of(0, 1));

              if (!userTenantPage.getContent().isEmpty()) {
                log.info("User ({}) is already affiliated", user.getUsername());
              } else {
                userTenantService.createPrimaryUserTenantAffiliation(consortiumId, consortiaTenant, user.getId(), user.getUsername());
                sendCreatePrimaryAffiliationEvent(consortiaTenant, user);
              }
            });
        }

        log.info("Successfully created primary affiliations for tenant {}", consortiaTenant.getId());
      } catch (Exception e) {
        log.error("Failed to create primary affiliations for tenant {}", consortiaTenant.getId(), e);
      }
    }));
  }

  @SneakyThrows
  private void sendCreatePrimaryAffiliationEvent(TenantEntity consortiaTenant, User user) {
    PrimaryAffiliationEvent affiliationEvent = createPrimaryAffiliationEvent(consortiaTenant, user);
    String data = objectMapper.writeValueAsString(affiliationEvent);
    kafkaService.send(KafkaService.Topic.CONSORTIUM_PRIMARY_AFFILIATION_CREATED, consortiaTenant.getConsortiumId().toString(), data);
  }

  private PrimaryAffiliationEvent createPrimaryAffiliationEvent(TenantEntity consortiaTenant, User user) {
    PrimaryAffiliationEvent event = new PrimaryAffiliationEvent();
    event.setId(UUID.randomUUID());
    event.setUserId(UUID.fromString(user.getId()));
    if (StringUtils.isNotBlank(user.getUsername())) { // for delete event username will be empty
      event.setUsername(user.getUsername());
    }
    event.setTenantId(consortiaTenant.getId());
    return event;
  }
}
