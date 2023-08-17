package org.folio.consortia.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.folio.consortia.client.CustomFieldsClient;
import org.folio.consortia.client.OkapiClient;
import org.folio.consortia.domain.dto.CustomField;
import org.folio.consortia.domain.dto.CustomFieldCollection;
import org.folio.consortia.domain.dto.CustomFieldType;
import org.folio.consortia.service.impl.CustomFieldServiceImpl;
import org.folio.spring.FolioExecutionContext;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
class CustomFieldServiceTest {
  @InjectMocks
  CustomFieldServiceImpl customFieldService;
  @Mock
  CustomFieldsClient customFieldsClient;
  @Mock
  OkapiClient okapiClient;
  @Mock
  FolioExecutionContext folioExecutionContext;

  private static final CustomField ORIGINAL_TENANT_ID_CUSTOM_FIELD = CustomField.builder()
    .name("originalTenantId")
    .entityType("user")
    .helpText("id of tenant where user created originally")
    .customFieldType(CustomFieldType.TEXTBOX_LONG)
    .visible(false)
    .build();


  @Test
  void shouldCreateCustomField() {
    CustomFieldCollection customFieldCollection = new CustomFieldCollection();
    customFieldCollection.setCustomFields(new ArrayList<>());
    customFieldCollection.setTotalRecords(0);
    CustomField customField = CustomField.builder().build();
    when(folioExecutionContext.getTenantId()).thenReturn("consortium");
    when(okapiClient.getModuleIds(any(), any(), any())).thenReturn(JsonNodeFactory.instance.arrayNode().add(JsonNodeFactory.instance.objectNode().put("id", "USERS")));
    when(customFieldsClient.getByQuery(any(), any())).thenReturn(customFieldCollection);
    Mockito.doNothing().when(customFieldsClient).postCustomFields(any(), any());
    customFieldService.createCustomField(customField);

    Mockito.verify(customFieldsClient).postCustomFields(any(), any());
    Mockito.verify(okapiClient, times(2)).getModuleIds(any(), any(), any());
  }

  @Test
  void shouldNotCreateNewCustomField() {
    CustomFieldCollection customFieldCollection = new CustomFieldCollection();
    customFieldCollection.setCustomFields(List.of(ORIGINAL_TENANT_ID_CUSTOM_FIELD));
    customFieldCollection.setTotalRecords(1);
    CustomField customField = CustomField.builder().name("originalTenantId").build();
    when(folioExecutionContext.getTenantId()).thenReturn("consortium");
    when(okapiClient.getModuleIds(any(), any(), any())).thenReturn(JsonNodeFactory.instance.arrayNode().add(JsonNodeFactory.instance.objectNode().put("id", "USERS")));
    when(customFieldsClient.getByQuery(any(), any())).thenReturn(customFieldCollection);
    customFieldService.createCustomField(customField);

    Mockito.verify(customFieldsClient, times(0)).postCustomFields(any(), any());
    Mockito.verify(customFieldsClient, times(1)).getByQuery(any(), any());
    Mockito.verify(okapiClient, times(1)).getModuleIds(any(), any(), any());
  }
}
