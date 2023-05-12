package org.folio.consortia.service;

import org.folio.consortia.domain.entity.ConsortiaConfigurationEntity;
import org.folio.consortia.domain.dto.ConsortiaConfiguration;
import org.springframework.messaging.MessageHeaders;

public interface ConsortiaConfigurationService {
  /**
   * Get Config based on requested tenant.
   * If process failed, it will specify error with tenant id.
   * There will be only one record
   *
   * @param requestedTenantId id of tenant in folio execution context
   * @return central tenant id
   */
  String getCentralTenantId(String requestedTenantId);

  ConsortiaConfiguration getConsortiaConfigurationByFolioExecutionContext();

  String getCentralTenantByIdByHeader(MessageHeaders messageHeaders);
  /**
   * Save new configuration with central tenant id as value.
   * This configuration will be stored in requested tenant schema
   *
   * @param centralTenantId id of central tenant for requested tenant
   */
  ConsortiaConfigurationEntity createConfiguration(String centralTenantId);

  ConsortiaConfiguration createConfigurationByFolioExecutionContext();
}
