package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.client.CustomFieldsClient;
import org.folio.consortia.domain.dto.CustomField;
import org.folio.consortia.service.CustomFieldService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class CustomFieldServiceImpl implements CustomFieldService {

  private final CustomFieldsClient customFieldsClient;

  @Override
  public void createCustomField(CustomField customField) {
    log.info("Creating custom-field with name {}.", customField.getName());
    customFieldsClient.postCustomFields(customField);
  }
}
