package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.rest.resource.SelfApi;
import org.folio.consortia.service.UserTenantService;
import org.folio.spring.FolioExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/consortia/{consortiumId}")
@RequiredArgsConstructor
public class SelfController implements SelfApi {

  @Autowired
  private final UserTenantService userTenantService;

  @Autowired
  private final FolioExecutionContext folioExecutionContext;

  @Override
  public ResponseEntity<UserTenantCollection> getSelfUserTenants(UUID consortiumId) {
    String token = folioExecutionContext.getToken();
    UUID userId = folioExecutionContext.getUserId();

    if (StringUtils.isBlank(token)) {
      throw new IllegalArgumentException("token is required");
    }

    UserTenantCollection userTenantCollection = userTenantService.getByUserId(consortiumId, userId, 0, Integer.MAX_VALUE);

    return ResponseEntity.ok(userTenantCollection);
  }
}
