package org.folio.consortia.client;


import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

import java.net.URI;

@FeignClient(name="dynamicUrlClient")
public interface DynamicUrlClient {
  @RequestLine("POST")
  void postRequest(URI url, Object payload);
}
