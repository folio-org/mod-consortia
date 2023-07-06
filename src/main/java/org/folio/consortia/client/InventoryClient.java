package org.folio.consortia.client;

import org.folio.spring.config.FeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory" , configuration = FeignClientConfiguration.class)
public interface InventoryClient {

  @GetMapping(value = "instances/{instanceId}")
  String getInstanceById(@PathVariable String instanceId);

  @PostMapping(value = "instances", consumes = MediaType.APPLICATION_JSON_VALUE)
  void saveInstance(@RequestBody String instance);
}
