package org.folio.consortia.service;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.folio.consortia.client.DynamicUrlClient;
import org.folio.consortia.domain.dto.Publication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PublishCoordinatorService {

  private final DynamicUrlClient dynamicUrlClient;
  @Async
  public CompletableFuture<Publication> publishRequest(Publication publication) {
    List<CompletableFuture<Void>> futures = publication.getTenants().parallelStream()
      .map(tenant -> makeTenantRequest(tenant, publication))
      .toList();
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
      .thenApply(v -> publication);
  }

  private CompletableFuture<Void> makeTenantRequest(String tenantId, Publication publication) {
    return CompletableFuture.supplyAsync(() -> buildTenantUrl(tenantId, publication))
      .thenAccept(url -> dynamicUrlClient.postRequest(url, publication.getPayload()));
  }

  @SneakyThrows
  private URI buildTenantUrl(String tenantId, Publication publication) {
    return new URI(publication.getUrl());
  }
}
