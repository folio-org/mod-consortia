package org.folio.consortia.controller;

import org.folio.consortia.domain.dto.Publication;
import org.folio.consortia.service.PublishCoordinatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PublishCoordinatorController implements org.folio.consortia.rest.resource.PublicationsApi {

  private final PublishCoordinatorService publishCoordinatorService;

  @Override
  public ResponseEntity<Publication> publishRequests(Publication publishCoordinator) {
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(publishCoordinatorService.publishRequest(publishCoordinator).join());
  }
}
