package org.folio.consortia.service;

import java.util.UUID;

public interface ConsortiumService {
  /**
   * Checks if a consortium exists.
   * @param consortiumId the consortium id
   */
  void checkConsortiumExists(UUID consortiumId);
}
