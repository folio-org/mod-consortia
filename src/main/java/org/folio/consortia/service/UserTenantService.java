package org.folio.consortia.service;

import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.dto.UserTenantCollection;

import java.util.UUID;

public interface UserTenantService {

  /**
   * Get user tenant associations collection by query
   *
   * @param offset the offset
   * @param limit  the limit
   * @return the user tenant associations collection
   */
  UserTenantCollection get(UUID consortiumId, Integer offset, Integer limit);

  /**
   * Get user tenant associations collection by user id
   *
   * @param userId the user id
   * @return the user tenant associations collection
   */
  UserTenantCollection getByUserId(UUID consortiumId, UUID userId, Integer offset, Integer limit);

  /**
   * Get a user tenant associations collection by username
   *
   * @param username the username
   * @param tenantId the tenant id
   * @return the user tenant associations collection
   */
  UserTenantCollection getByUsernameAndTenantId(UUID consortiumId, String username, String tenantId);

  /**
   * Get user tenant association by id
   *
   * @param id the id
   * @return the user tenant association
   */
  UserTenant getById(UUID consortiumId, UUID id);

}
