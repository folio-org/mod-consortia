package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.exception.TokenNotFoundException;
import org.folio.consortia.rest.resource.SelfApi;
import org.folio.consortia.service.UserTenantService;
import org.folio.spring.FolioExecutionContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/consortia/{consortiumId}")
@RequiredArgsConstructor
public class SelfController implements SelfApi {

  private final UserTenantService userTenantService;
  private final FolioExecutionContext folioExecutionContext;

  @Override
  public ResponseEntity<UserTenantCollection> getSelfUserTenants(UUID consortiumId) {
    String token = folioExecutionContext.getToken();
    UUID userId = folioExecutionContext.getUserId();

    if (StringUtils.isEmpty(token)) {
      throw new TokenNotFoundException();
    }

    UserTenantCollection userTenantCollection = userTenantService.getByUserId(consortiumId, userId, 0, Integer.MAX_VALUE);

    return ResponseEntity.ok(userTenantCollection);
  }
}
