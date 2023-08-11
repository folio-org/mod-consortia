package org.folio.consortia.service;

import org.folio.consortia.domain.dto.CustomField;

public interface CustomFieldService {

  /**
   * Creates custom-field.
   *
   * @param customField customField.
   *
   */
  void createCustomField(CustomField customField);
}
