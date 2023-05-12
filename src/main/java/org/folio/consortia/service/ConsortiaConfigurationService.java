package org.folio.consortia.service;

public interface ConsortiaConfigurationService {
  /**
   * Get Config based on requested tenant.
   * If process failed, it will specify error with tenant id.
   * There will be only one record, so don't need any parameter
   *
   * @return central tenant id
   */
  String getCentralTenant();

  /**
   * Save new configuration with central tenant id as value.
   * This configuration will be stored in requested tenant schema
   *
   * @param centralTenantId id of central tenant for requested tenant
   */
  void createConfiguration(String centralTenantId);
}
