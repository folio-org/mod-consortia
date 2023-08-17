package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.client.CustomFieldsClient;
import org.folio.consortia.client.OkapiClient;
import org.folio.consortia.domain.dto.CustomField;
import org.folio.consortia.service.CustomFieldService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;

import static java.lang.String.format;

@Service
@Log4j2
@RequiredArgsConstructor
public class CustomFieldServiceImpl implements CustomFieldService {
  private final FolioExecutionContext folioExecutionContext;

  private final OkapiClient okapiClient;
  public static final String OKAPI_URL = "http://_";
  private final CustomFieldsClient customFieldsClient;
  private static final String MOD_USERS = "mod-users";

  @Override
  public void createCustomField(CustomField customField) {
    log.info("Creating custom-field with name {}.", customField.getName());
    customFieldsClient.postCustomFields(getModuleId(MOD_USERS), customField);
  }

  @Cacheable(cacheNames = "moduleIds")
  public String getModuleId(String moduleName) {
    var tenantId = folioExecutionContext.getTenantId();
    var moduleNamesJson = okapiClient.getModuleIds(URI.create(OKAPI_URL), tenantId, moduleName);
    if (!moduleNamesJson.isEmpty()) {
      return moduleNamesJson.get(0).get("id").asText();
    }
    throw new NotFoundException(format("Module id not found for name: %s", moduleName));
  }
}
