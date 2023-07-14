package org.folio.consortia.service.impl;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingSetting;
import org.folio.consortia.domain.dto.SharingSettingResponse;
import org.folio.consortia.repository.SharingSettingRepository;
import org.folio.consortia.service.SharingSettingService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class SharingSettingServiceImpl implements SharingSettingService {
  private final SharingSettingRepository sharingSettingRepository;

  @Override
  public SharingSettingResponse start(UUID consortiumId, SharingSetting sharingSetting) {
    return null;
  }
}
