package org.folio.consortia.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.consortia.service.ConsortiaConfigurationService;
import org.folio.consortia.service.SharingInstanceService;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import static org.folio.consortia.utils.TenantContextUtils.createFolioExecutionContext;
import static org.folio.consortia.utils.TenantContextUtils.getHeaderValue;
import static org.folio.consortia.utils.TenantContextUtils.runInFolioContext;

@Log4j2
@Component
@RequiredArgsConstructor
public class ConsortiaSharingInstanceEventListener {

  public static final String CONSORTIUM_INSTANCE_SHARING_COMPLETE_LISTENER_ID = "consortium-instance-sharing-complete-listener-id";
  private final SharingInstanceService sharingInstanceService;
  private final ConsortiaConfigurationService configurationService;
  private final FolioModuleMetadata folioMetadata;

  @KafkaListener(
    id = CONSORTIUM_INSTANCE_SHARING_COMPLETE_LISTENER_ID,
    topicPattern = "#{folioKafkaProperties.listener['consortium-instance-sharing-complete'].topicPattern}",
    concurrency = "#{folioKafkaProperties.listener['consortium-instance-sharing-complete'].concurrency}",
    containerFactory = "kafkaListenerContainerFactory")
  public void handleConsortiumInstanceSharingCompleting(String data, MessageHeaders messageHeaders) {
    String centralTenantId = getCentralTenantByIdByHeader(messageHeaders);
    if (StringUtils.isNotBlank(centralTenantId)) {
      runInFolioContext(createFolioExecutionContext(messageHeaders, folioMetadata, centralTenantId), () ->
        sharingInstanceService.completePromotingLocalInstance(data));
    }
  }

  private String getCentralTenantByIdByHeader(MessageHeaders messageHeaders) {
    String requestedTenantId = getHeaderValue(messageHeaders, XOkapiHeaders.TENANT, null).get(0);
    // getting central tenant for this requested tenant from get central in its own schema
    try (var context = new FolioExecutionContextSetter(createFolioExecutionContext(
      messageHeaders, folioMetadata, requestedTenantId))) {
      return configurationService.getCentralTenantId(requestedTenantId);
    } catch (InvalidDataAccessResourceUsageException e) {
      log.info("Table consortia_configuration is not exists, because tenant: {} is not in consortium, DB message: {}, skipping...",
        requestedTenantId, e.getMessage());
    }

    return null;
  }
}
