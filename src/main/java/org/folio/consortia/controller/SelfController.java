package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.consortia.domain.dto.Payload;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.rest.resource.SelfApi;
import org.folio.consortia.service.UserTenantService;
import org.folio.consortia.utils.TokenUtils;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.integration.XOkapiHeaders;
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
    String token = folioExecutionContext
      .getOkapiHeaders()
      .get(XOkapiHeaders.TOKEN)
      .stream()
      .findFirst()
      .orElse("");

    if (token == "") {
      throw new IllegalArgumentException("token is required");
    }

    Payload payload = TokenUtils.parseToken(token);

    if (payload == null) {
      throw new IllegalArgumentException("token is invalid");
    }

    String userId = payload.getUser_id();
    UserTenantCollection userTenantCollection = userTenantService.getByUserId(consortiumId, UUID.fromString(userId), 0, Integer.MAX_VALUE);

    return ResponseEntity.ok(userTenantCollection);
  }
}
