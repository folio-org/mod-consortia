package org.folio.consortia.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.service.ConsortiaConfigurationService;
import org.folio.consortia.service.UserAffiliationService;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import static org.folio.consortia.utils.TenantContextUtils.createFolioExecutionContext;
import static org.folio.consortia.utils.TenantContextUtils.getHeaderValue;
import static org.folio.consortia.utils.TenantContextUtils.runInFolioContext;

@Log4j2
@Component
@RequiredArgsConstructor
public class ConsortiaEventListener {

  public static final String USER_CREATED_LISTENER_ID = "user-created-listener-id";
  public static final String USER_DELETED_LISTENER_ID = "user-deleted-listener-id";
  private final UserAffiliationService userAffiliationService;
  private final ConsortiaConfigurationService configurationService;
  private final FolioModuleMetadata folioMetadata;

  @KafkaListener(
    id = USER_CREATED_LISTENER_ID,
    topicPattern = "#{folioKafkaProperties.listener['user-created'].topicPattern}",
    concurrency = "#{folioKafkaProperties.listener['user-created'].concurrency}",
    containerFactory = "kafkaListenerContainerFactory")
  public void userCreatedListener(String data, MessageHeaders messageHeaders) {
    // to create affiliation in central tenant schema
    String centralTenantId = getCentralTenantByIdByHeader(messageHeaders);
    runInFolioContext(createFolioExecutionContext(messageHeaders, folioMetadata, centralTenantId), () ->
      userAffiliationService.createPrimaryUserAffiliation(data));
  }

  @KafkaListener(
    id = USER_DELETED_LISTENER_ID,
    topicPattern = "#{folioKafkaProperties.listener['user-deleted'].topicPattern}",
    concurrency = "#{folioKafkaProperties.listener['user-deleted'].concurrency}",
    containerFactory = "kafkaListenerContainerFactory")
  public void userDeletedListener(String data, MessageHeaders messageHeaders) {
    // to delete affiliation from central tenant schema
    String centralTenantId = getCentralTenantByIdByHeader(messageHeaders);
    runInFolioContext(createFolioExecutionContext(messageHeaders, folioMetadata, centralTenantId),
      () -> userAffiliationService.deletePrimaryUserAffiliation(data));
  }

  public String getCentralTenantByIdByHeader(MessageHeaders messageHeaders) {
    String requestedTenantId = getHeaderValue(messageHeaders, XOkapiHeaders.TENANT, null).get(0);
    String centralTenantId;

    // getting central tenant for this requested tenant from get central in its own schema
    try (var context = new FolioExecutionContextSetter(createFolioExecutionContext(
      messageHeaders, folioMetadata, requestedTenantId))) {
      centralTenantId = configurationService.getCentralTenantId(requestedTenantId);
    }

    return centralTenantId;
  }

}
