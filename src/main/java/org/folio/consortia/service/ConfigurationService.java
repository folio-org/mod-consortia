package org.folio.consortia.service;

public interface ConfigurationService {

  String getConfigValue(String configName, String tenantId);

  void saveConfiguration(String tenantId);

}
