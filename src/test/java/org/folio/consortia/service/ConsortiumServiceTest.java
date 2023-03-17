package org.folio.consortia.service;

import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.domain.repository.ConsortiumRepository;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.service.impl.ConsortiumServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
@EntityScan(basePackageClasses = TenantEntity.class)
class ConsortiumServiceTest {
  @InjectMocks
  private ConsortiumServiceImpl consortiumService;
  @Mock
  private ConsortiumRepository repository;

  @Test
  void shouldReturn404NotFoundException() {
    UUID consortiumId = UUID.randomUUID();
    when(repository.existsById(consortiumId)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> consortiumService.checkConsortiumExists(consortiumId));
  }
}
