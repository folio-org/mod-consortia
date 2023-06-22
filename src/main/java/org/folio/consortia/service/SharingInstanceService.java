package org.folio.consortia.service;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingInstance;

public interface SharingInstanceService {

  /**
   * Create Sharing Instance action
   * @param consortiumId UUID of consortium entity
   * @param sharingInstance the sharingInstanceDto
   * @return SharingInstanceDto
   */
  SharingInstance save(UUID consortiumId, SharingInstance sharingInstance);
}
