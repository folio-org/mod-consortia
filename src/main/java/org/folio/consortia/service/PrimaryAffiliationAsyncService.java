package org.folio.consortia.service;

import java.util.UUID;

import org.folio.consortia.domain.dto.SyncPrimaryAffiliationBody;

public interface PrimaryAffiliationAsyncService {

  /**
   * Create primary affiliation for user
   *
   * @param consortiumId               - consortium unique identifier
   * @param syncPrimaryAffiliationBody - consortia tenant record
   */
  void createPrimaryUserAffiliations(UUID consortiumId, SyncPrimaryAffiliationBody syncPrimaryAffiliationBody);

}
