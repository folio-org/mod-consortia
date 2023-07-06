package org.folio.consortia.controller;

import static org.folio.consortia.utils.TenantContextUtils.prepareContextForTenant;
import static org.folio.spring.scope.FolioExecutionScopeExecutionContextManager.getRunnableWithCurrentFolioContext;
import static org.folio.spring.scope.FolioExecutionScopeExecutionContextManager.getRunnableWithFolioContext;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.util.UUID;

import org.folio.consortia.domain.dto.SyncPrimaryAffiliationBody;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.rest.resource.TenantsApi;
import org.folio.consortia.service.SyncPrimaryAffiliationService;
import org.folio.consortia.service.TenantService;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/consortia/{consortiumId}")
@RequiredArgsConstructor
public class TenantController implements TenantsApi {

  private final TenantService service;
  private final TaskExecutor asyncTaskExecutor;
  private final SyncPrimaryAffiliationService syncPrimaryAffiliationService;
  private final FolioExecutionContext folioExecutionContext;
  @Override
  public ResponseEntity<TenantCollection> getTenants(UUID consortiumId, Integer offset, Integer limit) {
    return ResponseEntity.ok(service.get(consortiumId, offset, limit));
  }

  @Override
  public ResponseEntity<Tenant> saveTenant(UUID consortiumId, @Validated Tenant tenant, UUID adminUserId) {
    return ResponseEntity.status(CREATED).body(service.save(consortiumId, adminUserId, tenant));
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

  @Override
  public ResponseEntity<Void> syncPrimaryAffiliations(UUID consortiumId, String tenantId) {
//    var context  = prepareContextForTenant("diku", folioExecutionContext.getFolioModuleMetadata(), folioExecutionContext);
    asyncTaskExecutor.execute(getRunnableWithCurrentFolioContext(
      () -> syncPrimaryAffiliationService.syncPrimaryAffiliations(consortiumId, tenantId)));
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Override
  public ResponseEntity<Void> createPrimaryAffiliations(UUID consortiumId, String tenantId,
      SyncPrimaryAffiliationBody syncPrimaryAffiliationBody) {
//    try (var comtext  = new FolioExecutionContextSetter(prepareContextForTenant("diku", folioExecutionContext.getFolioModuleMetadata(), folioExecutionContext))) {
//      syncPrimaryAffiliationService.createPrimaryUserAffiliations(consortiumId, syncPrimaryAffiliationBody);
//    }
    var context  = prepareContextForTenant("diku", folioExecutionContext.getFolioModuleMetadata(), folioExecutionContext);

    asyncTaskExecutor.execute(getRunnableWithFolioContext(context,
      () -> syncPrimaryAffiliationService.createPrimaryUserAffiliations(consortiumId, syncPrimaryAffiliationBody)));
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
