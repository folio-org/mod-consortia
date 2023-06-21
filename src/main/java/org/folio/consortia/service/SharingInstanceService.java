package org.folio.consortia.service;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingInstanceAction;

public interface SharingInstanceService {

  /**
   * Create Shared Instance action
   * @param consortiumId UUID of consortium entity
   * @param sharedInstanceAction the sharedActionDto
   * @return SharedInstanceDto
   */
  SharingInstanceAction save(UUID consortiumId, SharingInstanceAction sharedInstanceAction);
}
