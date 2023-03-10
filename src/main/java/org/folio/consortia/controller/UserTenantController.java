package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.folio.consortia.service.UserTenantService;
import org.folio.pv.domain.dto.UserTenantCollection;
import org.folio.pv.domain.dto.UserTenant;
import org.folio.pv.rest.resource.UserTenantsApi;

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
  public ResponseEntity<UserTenantCollection> getUserTenants(UUID userId, String username, Integer offset, Integer limit) {
    return ResponseEntity.ok(userTenantService.get(userId, username, offset, limit));
  }


  @Override
  public ResponseEntity<UserTenant> getUserTenantByAssociationId(UUID associationId) {
    return ResponseEntity.ok(userTenantService.getById(associationId));
  }
}
