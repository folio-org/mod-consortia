package org.folio.consortia.domain.converter;

import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.entity.TenantEntity;

public class TenantConverter {
  private TenantConverter() {
  }

  public static Tenant toDto(TenantEntity source) {
    var tenantDto = new Tenant();
    tenantDto.setTenantId(source.getId());
    tenantDto.setTenantName(source.getName());
    return tenantDto;
  }
}
