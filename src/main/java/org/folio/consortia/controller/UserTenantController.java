package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.rest.resource.UserTenantsApi;
import org.folio.consortia.service.UserTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/consortia/{consortiumId}")
@RequiredArgsConstructor
public class UserTenantController implements UserTenantsApi {

  @Autowired
  private final UserTenantService userTenantService;

  @Override
  public ResponseEntity<UserTenantCollection> getUserTenants(UUID consortiumId, UUID userId, String username, String tenantId, Integer offset, Integer limit) {
    UserTenantCollection userTenantCollection;
    if (userId != null) {
      userTenantCollection = userTenantService.getByUserId(consortiumId, userId, offset, limit);
    } else if (StringUtils.isNotBlank(username)) {
      if (StringUtils.isBlank(tenantId)) {
        throw new IllegalArgumentException("tenantId is required when username is provided");
      }
      userTenantCollection = userTenantService.getByUsernameAndTenantId(consortiumId, username, tenantId);
    } else {
      userTenantCollection = userTenantService.get(consortiumId, offset, limit);
    }
    return ResponseEntity.status(HttpStatus.OK).body(userTenantCollection);
  }

  @Override
  public ResponseEntity<UserTenant> getUserTenantByAssociationId(UUID consortiumId, UUID associationId) {
    return ResponseEntity.status(HttpStatus.OK).body(userTenantService.getById(consortiumId, associationId));
  }
}
