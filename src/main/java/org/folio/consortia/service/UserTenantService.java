package org.folio.consortia.service;

import org.folio.pv.domain.dto.UserTenantCollection;

public interface UserTenantService {

  /**
   * Get user tenant associations collection by query
   * @param offset the offset
   * @param limit the limit
   * @return the user tenant associations collection
   */
  UserTenantCollection get(int offset, int limit);
}
