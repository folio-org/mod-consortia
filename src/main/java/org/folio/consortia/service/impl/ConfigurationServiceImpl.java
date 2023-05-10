package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.dto.ConfigurationEntry;
import org.folio.consortia.client.ConfigurationClient;
import org.folio.consortia.service.ConfigurationService;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {

  private static final String MODULE_NAME = "CONSORTIA";
  private static final String CONFIG_QUERY = "query=module==%s and configName==%s";


  private final ConfigurationClient client;

  @Override
  public String getConfigValue(String configName) {
    log.debug("getConfigValue:: Trying to get configValue by configName: {}", configName);
    try {
      String sql = String.format(CONFIG_QUERY, MODULE_NAME, configName);
      var configurations = client.getConfiguration(String.format(CONFIG_QUERY, MODULE_NAME, configName));

      if (configurations != null && configurations.getTotalRecords() > 0) {
        ConfigurationEntry configuration = configurations.getConfigurationEntries().get(0);
        log.info("getConfigValue:: configValue loaded by configName: {}", configName);
        return configuration.getValue();
      }
    } catch (Exception e) {
      log.error("Failed to get configuration={} : {}", configName, e.getMessage());
    }
    throw new IllegalStateException("You should create primary Affiliation Exception");
  }

  @Override
  public void saveConfiguration(String tenantId) {
    log.debug("saveConfiguration:: Trying to create configuration for central tenant id: {}", tenantId);
    try {
      ConfigurationEntry configuration = createConfiguration(tenantId);
      client.saveConfiguration(configuration);
      log.info("saveConfiguration:: Saved configuration with central tenant id: {}", tenantId);
    } catch (Exception e) {
      log.warn("Failed to create configuration={} : {}", tenantId, e.getMessage());
    }
  }

  private ConfigurationEntry  createConfiguration(String tenantId) {
    ConfigurationEntry configurationEntry = new ConfigurationEntry();
    configurationEntry.setModule(MODULE_NAME);
    configurationEntry.setValue(tenantId);
    configurationEntry.setConfigName("centralTenantId");
    return configurationEntry;
  }
}
