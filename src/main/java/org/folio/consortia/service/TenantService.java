package org.folio.consortia.service;

import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;

public interface TenantService {

  /**
   * Gets tenant collection.
   *
   * @param limit  the limit
   * @param offset the offset
   * @return tenant collection
   */
  TenantCollection get(Integer offset, Integer limit);

  /**
   * Inserts single tenant.
   *
   * @param tenantDto  the tenantDto
   * @return tenantDto
   */
  Tenant save(Tenant tenantDto);
}
