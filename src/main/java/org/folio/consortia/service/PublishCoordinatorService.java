package org.folio.consortia.service;

import org.folio.consortia.domain.dto.PublishCoordinator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class PublishCoordinatorService {
  public PublishCoordinator publishRequest(PublishCoordinator publishCoordinator) {
    List<CompletableFuture> futureList = new ArrayList<>();
    publishCoordinator.getTenants().stream()
      .map().toList();
    CompletableFuture.allOf(futureList).

    return publishCoordinator;
  }
}
