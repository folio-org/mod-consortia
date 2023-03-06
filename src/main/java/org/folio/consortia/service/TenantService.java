package org.folio.consortia.service;

import org.folio.pv.domain.dto.TenantCollection;

public interface TenantService {

  /**
   * Gets tenant collection by search query.
   *
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @return tenant collection
   */
  TenantCollection get(String query, Integer offset, Integer limit);
}
