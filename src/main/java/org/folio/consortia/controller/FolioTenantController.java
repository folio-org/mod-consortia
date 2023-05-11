package org.folio.consortia.controller;

import org.folio.consortia.config.FolioExecutionContextHelper;
import org.folio.spring.controller.TenantController;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;

@RestController("folioTenantController")
@RequestMapping
@Log4j2
public class FolioTenantController extends TenantController {

  private final FolioExecutionContextHelper contextHelper;

  public FolioTenantController(TenantService baseTenantService, FolioExecutionContextHelper contextHelper) {
    super(baseTenantService);
    this.contextHelper = contextHelper;
  }

  @Override
  public ResponseEntity<Void> postTenant(TenantAttributes tenantAttributes) {
    var tenantInit = super.postTenant(tenantAttributes);

    if (tenantInit.getStatusCode() == HttpStatus.NO_CONTENT) {
      try {
        contextHelper.registerTenant();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.internalServerError().build();
      }
    }

    return tenantInit;
  }
}
