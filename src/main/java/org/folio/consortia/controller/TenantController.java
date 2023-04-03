package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.rest.resource.TenantsApi;
import org.folio.consortia.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/consortia/{consortiumId}")
@RequiredArgsConstructor
public class TenantController implements TenantsApi {

  @Autowired
  private final TenantService service;

  @Override
  public ResponseEntity<TenantCollection> getTenants(UUID consortiumId, Integer offset, Integer limit) {
    return new ResponseEntity<>(service.get(consortiumId, offset, limit), OK);
  }

  @Override
  public ResponseEntity<Tenant> saveTenant(UUID consortiumId, @Validated Tenant tenant) {
    return new ResponseEntity<>(service.save(consortiumId, tenant), CREATED);
  }

  @Override
  public ResponseEntity<Tenant> updateTenant(UUID consortiumId, String tenantId, @Validated Tenant tenant) {
    return new ResponseEntity<>(service.update(consortiumId, tenantId, tenant), OK);
  }

  @Override
  public ResponseEntity<Void> deleteTenantById(UUID consortiumId, String tenantId) {
    service.delete(consortiumId, tenantId);
    return ResponseEntity.noContent().build();
  }
}
