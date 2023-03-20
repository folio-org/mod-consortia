package org.folio.consortia.domain.converter;

import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.entity.TenantEntity;
import org.springframework.core.convert.converter.Converter;

public class TenantEntityConverter implements Converter<Tenant, TenantEntity> {
  @Override
  public TenantEntity convert(Tenant source) {
    TenantEntity tenantEntity = new TenantEntity();
    tenantEntity.setId(source.getId());
    tenantEntity.setName(source.getName());
    return tenantEntity;
  }
}
