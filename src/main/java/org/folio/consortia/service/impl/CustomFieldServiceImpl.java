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
  public static final String QUERY_PATTERN_NAME = "name==%s";

  @Override
  public void createCustomField(CustomField customField) {
    log.info("Custom-field is not available, creating new custom-field with name {}.", customField.getName());
    customFieldsClient.postCustomFields(getModuleId(MOD_USERS), customField);
  }

  public CustomField getCustomFieldByName(String name) {
    log.info("Getting custom-field with name {}.", name);
    return customFieldsClient.getByQuery(getModuleId(MOD_USERS), format(QUERY_PATTERN_NAME, name))
      .getCustomFields().stream().filter(customField -> customField.getName().equals(name))
      .findFirst()
      .orElseThrow(() -> new NotFoundException(format("Custom field with name=%s not found", name)));
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
