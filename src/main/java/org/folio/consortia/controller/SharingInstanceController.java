package org.folio.consortia.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingInstanceAction;
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
  public ResponseEntity<SharingInstanceAction> saveSharingInstanceAction(UUID consortiumId, SharingInstanceAction sharingInstanceAction) {
    return ResponseEntity.status(CREATED).body(sharingInstanceService.save(consortiumId, sharingInstanceAction));
  }
}
