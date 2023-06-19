package org.folio.consortia.service.impl;

import static org.folio.consortia.utils.TenantContextUtils.prepareContextForTenant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.CollectionUtils;
import org.folio.consortia.domain.dto.PublicationRequest;
import org.folio.consortia.domain.dto.PublicationResponse;
import org.folio.consortia.exception.PublicationException;
import org.folio.consortia.service.PublicationService;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserTenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@AllArgsConstructor
public class PublicationServiceImpl implements PublicationService {
  private final TenantService tenantService;
  private final UserTenantService userTenantService;
  private final FolioExecutionContext folioExecutionContext;
  private final FolioModuleMetadata folioModuleMetadata;
  private final HttpRequestServiceImpl httpRequestService;
  private final TaskExecutor asyncTaskExecutor;

  @Override
  public PublicationResponse publishRequest(UUID consortiumId, PublicationRequest publication) {
    FolioExecutionContext currentTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();
    validatePublicationRequest(consortiumId, publication, currentTenantContext);

    PublicationResponse savedPublication = persistPublicationRecord();

    List<CompletableFuture<Object>> futures = new ArrayList<>();
    for (String tenantId : publication.getTenants()) {
      try (var context = new FolioExecutionContextSetter(prepareContextForTenant(tenantId, folioModuleMetadata, currentTenantContext))) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        executeAsyncTask(publication, tenantId, future);
        futures.add(future);
      }

    }
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
      .thenAccept(cf -> updatePublicationsStatus(futures, savedPublication));

    return buildPublicationResponse(savedPublication.getId());
  }

  private void executeAsyncTask(PublicationRequest publication, String tenantId, CompletableFuture<Object> future) {
    asyncTaskExecutor.execute(() -> {
      try {
        var response = httpRequestService.postRequest(publication.getUrl(), publication.getPayload());
        if (response.getStatusCode().is2xxSuccessful()) {
          log.info("publishRequest:: successfully called {}", publication.getUrl());
          future.complete(response.getBody());
        }
      } catch (Exception t) {
        log.error("Error making request on tenant {}", tenantId, t);
        future.completeExceptionally(t);
      }
    });
  }

  private PublicationResponse persistPublicationRecord() {
    var pr = new PublicationResponse()
      .id(UUID.randomUUID().toString())
      .status(PublicationResponse.StatusEnum.IN_PROGRESS);
    // TODO: persist publication record in database

    log.info("Created publication record {}", pr.getId());
    return pr;
  }

  private void updatePublicationsStatus(List<CompletableFuture<Object>> futures, PublicationResponse savedPublication) {
    var isCompletedWithExceptions = futures.stream()
      .anyMatch(CompletableFuture::isCompletedExceptionally);
    var updateStatus = isCompletedWithExceptions ? PublicationResponse.StatusEnum.ERROR : PublicationResponse.StatusEnum.SUCCESS;

    savedPublication.setStatus(updateStatus);

    // TODO: update publication record in database

    log.info("Updated publication record {} with status", savedPublication.getStatus());
  }

  private void validatePublicationRequest(UUID consortiumId, PublicationRequest publication, FolioExecutionContext context) {
    if (CollectionUtils.isEmpty(publication.getTenants())) {
      throw new PublicationException("Tenant list is empty");
    }
    tenantService.checkTenantsAndConsortiumExistsOrThrow(consortiumId, List.copyOf(publication.getTenants()));
    var userAffiliated = userTenantService.checkUserIfHasPrimaryAffiliationByUserId(consortiumId, context.getUserId().toString());
    if (!userAffiliated) {
      throw new PublicationException("User doesn't have primary affiliation");
    }
  }

  private PublicationResponse buildPublicationResponse(String publicationId) {
    return new PublicationResponse().id(publicationId)
      .status(PublicationResponse.StatusEnum.IN_PROGRESS);
  }

}
