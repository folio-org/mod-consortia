package org.folio.consortia.service;

import org.folio.pv.domain.dto.UserTenant;
import org.folio.pv.domain.dto.UserTenantCollection;

import java.util.UUID;

public interface UserTenantService {

  /**
   * Get user tenant associations collection by query
   *
   * @param offset the offset
   * @param limit  the limit
   * @return the user tenant associations collection
   */
  UserTenantCollection get(UUID userId, String username, Integer offset, Integer limit);

  UserTenant getById(UUID id);

}
