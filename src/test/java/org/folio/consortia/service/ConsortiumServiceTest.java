package org.folio.consortia.service;

import org.folio.consortia.domain.dto.Consortium;
import org.folio.consortia.domain.entity.ConsortiumEntity;
import org.folio.consortia.domain.repository.ConsortiumRepository;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.service.impl.ConsortiumServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
class ConsortiumServiceTest {
  @InjectMocks
  private ConsortiumServiceImpl consortiumService;
  @Mock
  private ConsortiumRepository repository;
  @Mock
  private ConversionService conversionService;

  @Test
  void shouldSaveConsortium() {
    ConsortiumEntity consortiumEntity = createConsortiumEntity("111841e3-e6fb-4191-8fd8-5674a5107c33", "Test");
    Consortium consortium = createConsortium("111941e3-e6fb-4191-8fd8-5674a5107c33", "Test");
    when(repository.save(any(ConsortiumEntity.class))).thenReturn(consortiumEntity);
    when(conversionService.convert(consortiumEntity, Consortium.class)).thenReturn(consortium);

    var consortium1 = consortiumService.save(consortium);
    Assertions.assertEquals(consortium, consortium1);
  }

  @Test
  void shouldGetErrorWhileSavingConsortium() {
    ConsortiumEntity consortiumEntity = createConsortiumEntity("111841e3-e6fb-4191-8fd8-5674a5107c33", "Test");
    Consortium consortium = createConsortium("111941e3-e6fb-4191-8fd8-5674a5107c33", "Test");
    when(repository.count()).thenThrow(ResourceAlreadyExistException.class);
    when(conversionService.convert(consortiumEntity, Consortium.class)).thenReturn(consortium);

    Assertions.assertThrows(org.folio.consortia.exception.ResourceAlreadyExistException.class,
      () -> consortiumService.save(consortium));
  }

  private ConsortiumEntity createConsortiumEntity(String id, String name) {
    ConsortiumEntity consortiumEntity = new ConsortiumEntity();
    consortiumEntity.setId(UUID.fromString(id));
    consortiumEntity.setName(name);
    return consortiumEntity;
  }

  private Consortium createConsortium(String id, String name) {
    Consortium consortium = new Consortium();
    consortium.setId(UUID.fromString(id));
    consortium.setName(name);
    return consortium;
  }
}
