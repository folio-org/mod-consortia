package org.folio.consortia.service;

import org.folio.consortia.client.CustomFieldsClient;
import org.folio.consortia.domain.dto.CustomField;
import org.folio.consortia.service.impl.CustomFieldServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
public class CustomFieldServiceTest {
  @InjectMocks
  CustomFieldServiceImpl customFieldService;
  @Mock
  CustomFieldsClient customFieldsClient;

  @Test
  void shouldCreateUser() {
    CustomField customField = CustomField.builder().build();
    Mockito.doNothing().when(customFieldsClient).postCustomFields(customField);
    customFieldService.createCustomField(customField);
    Mockito.verify(customFieldsClient).postCustomFields(customField);
  }
}
