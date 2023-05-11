package org.folio.consortia.service;

import org.folio.consortia.client.ConfigurationClient;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.service.impl.ConfigurationServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.folio.consortia.utils.EntityUtils.createConfigurationEntryCollection;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
class ConfigurationServiceTest {
  public static final String CENTRAL_TENANT_ID = "diku";
  public static final String TENANT_ID = "tenant_a";
  public static final String CONFIG_NAME = "centralTenantId";
  @InjectMocks
  ConfigurationServiceImpl configurationService;
  @Mock
  ConfigurationClient client;

  @Test
  void shouldGetConfigValue() {
    when(client.getConfiguration(anyString())).thenReturn(createConfigurationEntryCollection());

    String actualCentralTenantId = configurationService.getConfigValue(CONFIG_NAME, TENANT_ID);
    Assertions.assertEquals(CENTRAL_TENANT_ID, actualCentralTenantId);
  }

  @Test
  void shouldSaveConfigValue() {
    doNothing().when(client).saveConfiguration(any());

    configurationService.saveConfiguration(CENTRAL_TENANT_ID);
    verify(client, times(1)).saveConfiguration(any());
  }

  @Test
  void shouldThrowCentralTenantNotFoundErrorWhileGetConfigValue() {
    when(client.getConfiguration(anyString())).thenReturn(null);

    Assertions.assertThrows(org.folio.consortia.exception.ResourceNotFoundException.class,
      () -> configurationService.getConfigValue(CONFIG_NAME, TENANT_ID));
  }

}
