package org.folio.consortia.service;

import org.folio.consortia.repository.ConsortiaConfigurationRepository;
import org.folio.consortia.service.impl.ConsortiaConfigurationServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.folio.consortia.utils.EntityUtils.createConsortiaConfiguration;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
class ConsortiaConfigurationServiceTest {
  public static final String CENTRAL_TENANT_ID = "diku";

  @InjectMocks
  ConsortiaConfigurationServiceImpl configurationService;
  @Mock
  ConsortiaConfigurationRepository configurationRepository;

  @Test
  void shouldGetConfigValue() {
    when(configurationRepository.save(any())).thenReturn(createConsortiaConfiguration(CENTRAL_TENANT_ID));

    String actualCentralTenantId = configurationService.getCentralTenant();
    Assertions.assertEquals(CENTRAL_TENANT_ID, actualCentralTenantId);
  }

  @Test
  void shouldSaveConfigValue() {
    doNothing().when(configurationRepository).save(any());

    configurationService.createConfiguration(CENTRAL_TENANT_ID);
    verify(configurationRepository, times(1)).save(any());
  }

//  @Test
//  void shouldThrowCentralTenantNotFoundErrorWhileGetConfigValue() {
//    when(client.getConfiguration(anyString())).thenReturn(null);
//
//    Assertions.assertThrows(org.folio.consortia.exception.ResourceNotFoundException.class,
//      () -> configurationService.getConfigValue(CONFIG_NAME, TENANT_ID));
//  }

}
