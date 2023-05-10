package org.folio.consortia.service;

public interface ConfigurationService {

  String getConfigValue(String configName);

  void saveConfiguration(String tenantId);

}
