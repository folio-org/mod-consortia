package org.folio.consortia.client;

import org.folio.consortia.domain.dto.CustomField;
import org.folio.spring.config.FeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "custom-fields", configuration = FeignClientConfiguration.class)
public interface CustomFieldsClient {

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  void postCustomFields(@RequestBody CustomField entity);
}
