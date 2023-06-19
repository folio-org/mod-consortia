package org.folio.consortia.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.folio.consortia.service.HttpRequestService;
import org.folio.spring.FolioExecutionContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class HttpRequestServiceImpl implements HttpRequestService {
  private final RestTemplate restTemplate = new RestTemplate();
  private final FolioExecutionContext folioExecutionContext;

  public ResponseEntity<Object> postRequest(String url, Object payload) {
    FolioExecutionContext currentTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();
    var headers = convertHeadersToMultiMap(folioExecutionContext.getAllHeaders());
    HttpEntity<Object> httpEntity = new HttpEntity<>(payload, headers);
    var absUrl = currentTenantContext.getOkapiUrl() + url;

    return restTemplate.exchange(absUrl, HttpMethod.POST, httpEntity, Object.class);
  }

  private HttpHeaders convertHeadersToMultiMap(Map<String, Collection<String>> contextHeaders) {
    HttpHeaders multimapHeaders = new HttpHeaders();
    contextHeaders.forEach((key, value) -> multimapHeaders.put(key, (List<String>) value));

    return multimapHeaders;
  }

}
