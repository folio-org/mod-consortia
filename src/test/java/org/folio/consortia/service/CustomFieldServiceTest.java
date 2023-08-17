package org.folio.consortia.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.folio.consortia.client.CustomFieldsClient;
import org.folio.consortia.client.OkapiClient;
import org.folio.consortia.domain.dto.CustomField;
import org.folio.consortia.service.impl.CustomFieldServiceImpl;
import org.folio.spring.FolioExecutionContext;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
public class CustomFieldServiceTest {
  @InjectMocks
  CustomFieldServiceImpl customFieldService;
  @Mock
  CustomFieldsClient customFieldsClient;
  @Mock
  OkapiClient okapiClient;
  @Mock
  FolioExecutionContext folioExecutionContext;

  @Test
  void shouldCreateCustomField() {
    CustomField customField = CustomField.builder().build();
    when(folioExecutionContext.getTenantId()).thenReturn("consortium");
    when(okapiClient.getModuleIds(any(), any(), any())).thenReturn(JsonNodeFactory.instance.arrayNode().add(JsonNodeFactory.instance.objectNode().put("id", "USERS")));
    Mockito.doNothing().when(customFieldsClient).postCustomFields(any(), any());
    customFieldService.createCustomField(customField);

    Mockito.verify(customFieldsClient).postCustomFields(any(), any());
    Mockito.verify(okapiClient).getModuleIds(any(), any(), any());
  }
}
