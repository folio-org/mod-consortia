package org.folio.consortia.service;


import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.domain.dto.Tenant;

import java.util.UUID;

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
   * Update tenant.
   * @param consortiumId the consortium id
   * @param tenant the tenant
   * @return tenant
   */
  Tenant update(UUID consortiumId, Tenant tenant);
}
