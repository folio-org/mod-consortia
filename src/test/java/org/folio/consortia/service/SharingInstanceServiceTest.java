package org.folio.consortia.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.consortia.utils.EntityUtils.createSharingInstance;
import static org.folio.consortia.utils.EntityUtils.createSharingInstanceEntity;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingInstance;
import org.folio.consortia.domain.entity.SharingInstanceEntity;
import org.folio.consortia.repository.ConsortiumRepository;
import org.folio.consortia.repository.SharingInstanceRepository;
import org.folio.consortia.service.impl.SharingInstanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
class SharingInstanceServiceTest {

  private static final UUID instanceIdentifier = UUID.fromString("5b157ec2-8134-4363-a7b1-c9531a7c6a54");
  @InjectMocks
  private SharingInstanceServiceImpl sharingInstanceService;
  @Mock
  private ConsortiumRepository consortiumRepository;
  @Mock
  private ConsortiumService consortiumService;
  @Mock
  private SharingInstanceRepository sharingInstanceRepository;
  @Mock
  private ConversionService conversionService;

  @Test
  void shouldSaveSharingInstance() {
    SharingInstance sharingInstance = createSharingInstance(instanceIdentifier, "college", "mobius");
    SharingInstanceEntity savedSharingInstance = createSharingInstanceEntity(instanceIdentifier, "college", "mobius");

    when(consortiumRepository.existsById(any())).thenReturn(true);
    when(conversionService.convert(any(), any())).thenReturn(toDto(savedSharingInstance));
    when(sharingInstanceRepository.save(any())).thenReturn(savedSharingInstance);

    var expectedSharingInstance = createSharingInstance(instanceIdentifier, "college", "mobius");
    var actualSharingInstance = sharingInstanceService.save(UUID.randomUUID(), sharingInstance);

    assertThat(actualSharingInstance.getInstanceIdentifier()).isEqualTo(expectedSharingInstance.getInstanceIdentifier());
    assertThat(actualSharingInstance.getSourceTenantId()).isEqualTo(expectedSharingInstance.getSourceTenantId());
    assertThat(actualSharingInstance.getTargetTenantId()).isEqualTo(expectedSharingInstance.getTargetTenantId());

    verify(sharingInstanceRepository, times(1)).save(any());
  }


  private SharingInstance toDto(SharingInstanceEntity entity) {
    SharingInstance sharingInstance = new SharingInstance();
    sharingInstance.setInstanceIdentifier(entity.getInstanceId());
    sharingInstance.setSourceTenantId(entity.getSourceTenantId());
    sharingInstance.setTargetTenantId(entity.getTargetTenantId());
    sharingInstance.setStatus(String.valueOf(entity.getStatus()));
    return sharingInstance;
  }
}
