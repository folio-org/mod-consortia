package org.folio.consortia.service;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingInstance;

public interface SharingInstanceService {

  /**
   * Get a sharing instance by action ID
   * @param consortiumId  id of consortium
   * @param actionId id of sharing instance
   * @return SharingInstanceDto
   */
  SharingInstance getById(UUID consortiumId, UUID actionId);

  /**
   * Create Sharing Instance action
   * @param consortiumId UUID of consortium entity
   * @param sharingInstance the sharingInstanceDto
   * @return SharingInstanceDto
   */
  SharingInstance save(UUID consortiumId, SharingInstance sharingInstance);
}
