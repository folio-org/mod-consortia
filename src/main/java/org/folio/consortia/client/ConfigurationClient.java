package org.folio.consortia.client;

import org.folio.consortia.domain.dto.ConfigurationEntry;
import org.folio.consortia.domain.dto.ConfigurationEntryCollection;
import org.folio.spring.config.FeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "configurations/entries",  configuration = FeignClientConfiguration.class)
public interface ConfigurationClient {

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  ConfigurationEntryCollection getConfiguration(@RequestParam("query") String query);

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  void saveConfiguration(@RequestBody ConfigurationEntry configuration);
}
