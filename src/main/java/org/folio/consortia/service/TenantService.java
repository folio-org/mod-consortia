package org.folio.consortia.service;

import org.folio.pv.domain.dto.TenantCollection;

public interface TenantService {

  /**
   * Gets tenant collection by search query.
   *
   * @return tenant collection
   */
  TenantCollection get();
}
