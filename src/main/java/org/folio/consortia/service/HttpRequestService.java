package org.folio.consortia.service;

import org.springframework.http.ResponseEntity;

public interface HttpRequestService {
  ResponseEntity<Object> postRequest(String url, Object payload);
}
