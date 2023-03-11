package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.rest.resource.UserTenantsApi;
import org.folio.consortia.service.UserTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/consortia")
@RequiredArgsConstructor
public class UserTenantController implements UserTenantsApi {

  @Autowired
  private final UserTenantService userTenantService;

  @Override
  public ResponseEntity<UserTenantCollection> getUserTenants(UUID userId, String username, String tenantId,
                                                             Integer offset, Integer limit) {
    UserTenantCollection userTenantCollection;
    if (userId != null) {
      userTenantCollection = userTenantService.getByUserId(userId);
    } else if (username != null) {
      userTenantCollection = userTenantService.getByUsername(username, tenantId);
    } else {
      userTenantCollection = userTenantService.get(offset, limit);
    }
    return ResponseEntity.ok(userTenantCollection);
  }


  @Override
  public ResponseEntity<UserTenant> getUserTenantByAssociationId(UUID associationId) {
    return ResponseEntity.ok(userTenantService.getById(associationId));
  }
}
