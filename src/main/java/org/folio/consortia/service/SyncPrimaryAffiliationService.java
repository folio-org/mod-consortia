package org.folio.consortia.service;

import java.util.UUID;

public interface SyncPrimaryAffiliationService {
  void syncPrimaryAffiliations(UUID consortiumId, String syncPrimaryAffiliationBody);
}
