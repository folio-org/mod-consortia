package org.folio.consortia.service;

import org.folio.consortia.domain.entity.ConsortiaConfigurationEntity;
import org.folio.consortia.repository.ConsortiaConfigurationRepository;
import org.folio.consortia.service.impl.ConsortiaConfigurationServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.folio.consortia.utils.EntityUtils.createConsortiaConfiguration;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
class ConsortiaConfigurationServiceTest {
  private static final String CENTRAL_TENANT_ID = "diku";
  private static final String TENANT_ID = "testtenant1";

  @InjectMocks
  ConsortiaConfigurationServiceImpl configurationService;
  @Mock
  ConsortiaConfigurationRepository configurationRepository;

  @Test
  void shouldGetConfigValue() {
    List<ConsortiaConfigurationEntity> configurationEntityList = List.of(createConsortiaConfiguration(CENTRAL_TENANT_ID));

    when(configurationRepository.findAll()).thenReturn(configurationEntityList);
    String actualCentralTenantId = configurationService.getCentralTenantId(TENANT_ID);

    Assertions.assertEquals(CENTRAL_TENANT_ID, actualCentralTenantId);
  }

  @Test
  void shouldSaveConfigValue() {
    ConsortiaConfigurationEntity configuration = createConsortiaConfiguration(CENTRAL_TENANT_ID);

    when(configurationRepository.save(any())).thenReturn(configuration);
    when(configurationRepository.count()).thenReturn(0L);

    configurationService.createConfiguration(CENTRAL_TENANT_ID);

    verify(configurationRepository, times(1)).save(any());
  }

  @Test
  void shouldThrowCentralTenantNotFoundErrorWhileGetConfigValue() {
    ConsortiaConfigurationEntity configuration = createConsortiaConfiguration(CENTRAL_TENANT_ID);

    when(configurationRepository.save(any())).thenReturn(configuration);
    when(configurationRepository.count()).thenReturn(1L);

    Assertions.assertThrows(org.folio.consortia.exception.ResourceAlreadyExistException.class,
      () -> configurationService.createConfiguration(CENTRAL_TENANT_ID));

    verify(configurationRepository, times(0)).save(any());

  }

}
