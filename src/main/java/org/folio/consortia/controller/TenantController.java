package org.folio.consortia.controller;

import lombok.RequiredArgsConstructor;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.rest.resource.TenantsApi;
import org.folio.consortia.service.ConfigurationService;
import org.folio.consortia.service.TenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.folio.consortia.utils.TenantContextUtils.prepareContextForTenant;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/consortia/{consortiumId}")
@RequiredArgsConstructor
public class TenantController implements TenantsApi {

  private final TenantService service;
  private final ConfigurationService configurationService;
  private final FolioExecutionContext folioExecutionContext;
  private final FolioModuleMetadata folioModuleMetadata;

  @Override
  public ResponseEntity<TenantCollection> getTenants(UUID consortiumId, Integer offset, Integer limit) {
    return ResponseEntity.ok(service.get(consortiumId, offset, limit));
  }

  @Override
  public ResponseEntity<Tenant> saveTenant(UUID consortiumId, @Validated Tenant tenant) {
    FolioExecutionContext currentTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();
    // query to tenant table to get central tenant id,
    // if not central, throw new error
    String centralTenantId = service.getCentralTenantId(); // central tenant id is checking for null in TenantService
    // prepare context x-okapi-tenant - tenantId == tenant.getId()
    // call to mod-config to save
    try (var context = new FolioExecutionContextSetter(prepareContextForTenant(tenant.getId(), currentTenantContext, folioModuleMetadata))) {
      configurationService.saveConfiguration(centralTenantId);
    }
    try (var context = new FolioExecutionContextSetter(prepareContextForTenant(centralTenantId, currentTenantContext, folioModuleMetadata))) {
      return ResponseEntity.status(CREATED).body(service.save(consortiumId, tenant));
    }
  }

  @Override
  public ResponseEntity<Tenant> updateTenant(UUID consortiumId, String tenantId, @Validated Tenant tenant) {
    return ResponseEntity.ok(service.update(consortiumId, tenantId, tenant));
  }

  @Override
  public ResponseEntity<Void> deleteTenantById(UUID consortiumId, String tenantId) {
    service.delete(consortiumId, tenantId);
    return ResponseEntity.status(NO_CONTENT).build();
  }
}
