package org.folio.consortia.messaging.listener;

import static org.folio.consortia.utils.TenantContextUtils.getFolioExecutionContextCreatePrimaryAffiliationEvent;
import static org.folio.consortia.utils.TenantContextUtils.runInFolioContext;

import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.service.UserAffiliationService;
import org.folio.spring.FolioModuleMetadata;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public class ConsortiaEventListener {

  public static final String USER_CREATED_LISTENER_ID = "user-created-listener-id";
  public static final String USER_DELETED_LISTENER_ID = "user-deleted-listener-id";

  private final UserAffiliationService userAffiliationService;
  private final FolioModuleMetadata moduleMetadata;

  @KafkaListener(
    id = USER_CREATED_LISTENER_ID,
    topicPattern = "FOLIO.Default.diku.USER_CREATED",
    concurrency = "1",
    containerFactory = "kafkaListenerContainerFactory")
  public void userCreatedListener(String data, MessageHeaders messageHeaders) {
    log.info("Received USER_CREATED message: {}", data);
    runInFolioContext(getFolioExecutionContextCreatePrimaryAffiliationEvent(messageHeaders, moduleMetadata),
      () -> userAffiliationService.createPrimaryUserAffiliation(data));
  }

  @KafkaListener(
    id = USER_DELETED_LISTENER_ID,
    topicPattern = "FOLIO.Default.diku.USER_DELETED",
    concurrency = "1",
    containerFactory = "kafkaListenerContainerFactory")
  public void userDeletedListener(String data, MessageHeaders messageHeaders) {
    log.info("Received USER_DELETED message: {}", data);
    runInFolioContext(getFolioExecutionContextCreatePrimaryAffiliationEvent(messageHeaders, moduleMetadata),
      () -> userAffiliationService.deletePrimaryUserAffiliation(data));
  }


}
