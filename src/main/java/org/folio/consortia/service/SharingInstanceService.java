package org.folio.consortia.service;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingInstance;

public interface SharingInstanceService {

  /**
   * Create Shared Instance action
   * @param consortiumId UUID of consortium entity
   * @param sharedInstance the sharedActionDto
   * @return SharedInstanceDto
   */
  SharingInstance save(UUID consortiumId, SharingInstance sharedInstance);
}
