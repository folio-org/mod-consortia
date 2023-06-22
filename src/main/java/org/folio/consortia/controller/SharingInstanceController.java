package org.folio.consortia.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingInstance;
import org.folio.consortia.rest.resource.SharingApi;
import org.folio.consortia.service.SharingInstanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SharingInstanceController implements  SharingApi{

  private final SharingInstanceService sharingInstanceService;

  @Override
  public ResponseEntity<SharingInstance> saveSharingInstance(UUID consortiumId, SharingInstance sharingInstance) {
    return ResponseEntity.status(CREATED).body(sharingInstanceService.save(consortiumId, sharingInstance));
  }
}
