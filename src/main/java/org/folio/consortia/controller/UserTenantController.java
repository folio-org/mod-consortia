package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.rest.resource.ConsortiumIdApi;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.UserTenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/consortia")
@RequiredArgsConstructor
public class UserTenantController implements ConsortiumIdApi {

  private final UserTenantService userTenantService;
  private final ConsortiumService consortiumService;


  @Override
  public ResponseEntity<UserTenantCollection> getUserTenants(UUID consortiumId, UUID userId, String username,
                                                             String tenantId, Integer offset, Integer limit) {
    consortiumService.checkConsortiumExists(consortiumId);
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
    return ResponseEntity.ok(userTenantCollection);
  }

  @Override
  public ResponseEntity<UserTenant> getUserTenantByAssociationId(UUID consortiumId, UUID associationId) {
    consortiumService.checkConsortiumExists(consortiumId);
    return ResponseEntity.ok(userTenantService.getById(consortiumId, associationId));
  }


}
