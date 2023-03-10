package org.folio.consortia.domain.converter;

import org.folio.consortia.domain.entity.Tenant;

public class TenantConverter {

  private TenantConverter() {
    throw new IllegalArgumentException("Failed to convert");
  }

  public static org.folio.pv.domain.dto.Tenant toDto(Tenant tenant) {
    var tenantDto = new org.folio.pv.domain.dto.Tenant();
    tenantDto.setTenantId(tenant.getId());
    tenantDto.setTenantName(tenant.getName());
    return tenantDto;
  }
}
