package org.folio.consortia.messaging.listener;

import org.folio.consortia.service.ConfigurationService;
import org.folio.consortia.service.UserAffiliationService;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static org.folio.consortia.utils.TenantContextUtils.*;

@Log4j2
@Component
@RequiredArgsConstructor
public class ConsortiaEventListener {

  public static final String USER_CREATED_LISTENER_ID = "user-created-listener-id";
  public static final String USER_DELETED_LISTENER_ID = "user-deleted-listener-id";
  private final UserAffiliationService userAffiliationService;
  private final FolioModuleMetadata moduleMetadata;
  private final ConfigurationService configurationService;

  @KafkaListener(
    id = USER_CREATED_LISTENER_ID,
    topicPattern = "#{folioKafkaProperties.listener['user-created'].topicPattern}",
    concurrency = "#{folioKafkaProperties.listener['user-created'].concurrency}",
    containerFactory = "kafkaListenerContainerFactory")
  public void userCreatedListener(String data, MessageHeaders messageHeaders) {
    String centralTenantId = getCentralTenantId(messageHeaders);
    runInFolioContext(getFolioExecutionContextCreatePrimaryAffiliationEvent(messageHeaders, moduleMetadata, centralTenantId),
      () -> userAffiliationService.createPrimaryUserAffiliation(data));
  }

  @KafkaListener(
    id = USER_DELETED_LISTENER_ID,
    topicPattern = "#{folioKafkaProperties.listener['user-deleted'].topicPattern}",
    concurrency = "#{folioKafkaProperties.listener['user-deleted'].concurrency}",
    containerFactory = "kafkaListenerContainerFactory")
  public void userDeletedListener(String data, MessageHeaders messageHeaders) {
    String centralTenantId = getCentralTenantId(messageHeaders);
    runInFolioContext(getFolioExecutionContextDeletePrimaryAffiliationEvent(messageHeaders, moduleMetadata, centralTenantId),
      () -> userAffiliationService.deletePrimaryUserAffiliation(data));
  }

  private String getCentralTenantId(MessageHeaders messageHeaders) {
    // ! I should check messageHeader have correct tenant id (university)
    // get tenant id from messageHeaders
    String tenantId = getHeaderValue(messageHeaders, XOkapiHeaders.TENANT, null).get(0);
    // prepare folio execution context with this tenant id
    // call mod-config
    String centralTenantId;
    try (var context = new FolioExecutionContextSetter(getFolioExecutionContextCreatePrimaryAffiliationEvent(messageHeaders, moduleMetadata, tenantId))) {
      centralTenantId = configurationService.getConfigValue("centralTenantId", tenantId);
    }
    // if we couldn't find central tenant id, just "return;"
    // prepare folio execution context with central tenant id (using mod-config)
    return centralTenantId;
  }

}
