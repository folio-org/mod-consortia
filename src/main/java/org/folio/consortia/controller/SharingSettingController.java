package org.folio.consortia.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingSetting;
import org.folio.consortia.domain.dto.SharingSettingResponse;
import org.folio.consortia.rest.resource.SharingApi;
import org.folio.consortia.service.SharingSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/consortia/{consortiumId}")
@Log4j2
@RequiredArgsConstructor
public class SharingSettingController implements SharingApi {

  private final SharingSettingService sharingsettingService;

  @Override
  public ResponseEntity<SharingSettingResponse> startSharingSetting(UUID consortiumId, SharingSetting sharingSetting) {
    return ResponseEntity.status(CREATED).body(sharingsettingService.start(consortiumId, sharingSetting));
  }
}
