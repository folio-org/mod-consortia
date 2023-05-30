package org.folio.consortia.controller;

import org.folio.consortia.domain.dto.PublishCoordinator;
import org.folio.consortia.service.PublishCoordinatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PublishCoordinatorController implements org.folio.consortia.rest.resource.PublishCoordinatorApi {

  private final PublishCoordinatorService publishCoordinatorService;
  @Override
  public ResponseEntity<PublishCoordinator> publishRequests(PublishCoordinator publishCoordinator) {
    return new ResponseEntity<>(publishCoordinatorService.publishRequest(publishCoordinator));
  }
}
