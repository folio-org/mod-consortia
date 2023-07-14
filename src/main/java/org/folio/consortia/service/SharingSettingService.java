package org.folio.consortia.service;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingSetting;
import org.folio.consortia.domain.dto.SharingSettingResponse;

public interface SharingSettingService {

  /**
   * Start sharing setting
   * @param consortiumId UUID of consortium entity
   * @param sharingSetting the sharingSettingDTO (data transfer object)
   * @return SharingInstanceDto
   */
  SharingSettingResponse start(UUID consortiumId, SharingSetting sharingSetting);

}
