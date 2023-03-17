package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.domain.dto.TenantCollection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/consortia")
@RequiredArgsConstructor
public class TenantController implements org.folio.consortia.rest.resource.ConsoritumIdApi {
  private final TenantService service;
  private final ConsortiumService consortiumService;

  @Override
  public ResponseEntity<TenantCollection> getTenants(UUID consortiumId, Integer offset, Integer limit) {
    consortiumService.checkConsortiumExists(consortiumId);
    return ResponseEntity.ok(service.get(offset, limit));
  }

  @Override
  public ResponseEntity<Tenant> updateTenant(UUID consortiumId, Tenant tenant) {
    consortiumService.checkConsortiumExists(consortiumId);
    return ResponseEntity.ok(service.update(consortiumId, tenant));
  }
}
