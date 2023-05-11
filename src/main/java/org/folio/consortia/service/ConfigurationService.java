package org.folio.consortia.service;

public interface ConfigurationService {

  /**
   * Get Config value by configName. If process failed,
   * it will specify error with tenant id
   *
   * @param configName saved name for configName field in mod-configuration
   * @param tenantId   id of tenant
   * @return central tenant id
   */
  String getConfigValue(String configName, String tenantId);

  /**
   * Save new configuration with central tenant id as value
   *
   * @param centralTenantId id of central tenant for request tenant
   */
  void saveConfiguration(String centralTenantId);

}
