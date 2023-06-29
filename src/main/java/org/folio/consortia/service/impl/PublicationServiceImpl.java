package org.folio.consortia.service.impl;

import static org.folio.consortia.exception.PublicationException.PRIMARY_AFFILIATION_NOT_EXISTS;
import static org.folio.consortia.exception.PublicationException.TENANT_LIST_EMPTY;
import static org.folio.consortia.utils.TenantContextUtils.prepareContextForTenant;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpException;
import org.folio.consortia.domain.dto.PublicationRequest;
import org.folio.consortia.domain.dto.PublicationResponse;
import org.folio.consortia.domain.entity.PublicationStatusEntity;
import org.folio.consortia.domain.entity.PublicationTenantRequestEntity;
import org.folio.consortia.exception.PublicationException;
import org.folio.consortia.repository.PublicationStatusRepository;
import org.folio.consortia.repository.PublicationTenantRequestRepository;
import org.folio.consortia.service.HttpRequestService;
import org.folio.consortia.service.PublicationService;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserTenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@AllArgsConstructor
public class PublicationServiceImpl implements PublicationService {
  private final TenantService tenantService;
  private final UserTenantService userTenantService;
  private final FolioExecutionContext folioExecutionContext;
  private final FolioModuleMetadata folioModuleMetadata;
  private final HttpRequestService httpRequestService;
  private final TaskExecutor asyncTaskExecutor;

  private final PublicationStatusRepository publicationStatusRepository;
  private final PublicationTenantRequestRepository publicationTenantRequestRepository;
  private final ObjectMapper objectMapper;
  private static final int MAX_ACTIVE_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

  @Override
  @SneakyThrows
  public PublicationResponse publishRequest(UUID consortiumId, PublicationRequest publicationRequest) {
    FolioExecutionContext centralTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();
    validatePublicationRequest(consortiumId, publicationRequest, centralTenantContext);

    final PublicationStatusEntity savedPublicationEntity = persistPublicationRecord(centralTenantContext.getUserId(), publicationRequest.getTenants().size());

    List<CompletableFuture<PublicationTenantRequestEntity>> futures = new ArrayList<>();
    Semaphore semaphore = new Semaphore(MAX_ACTIVE_THREADS);
    for (String tenantId : publicationRequest.getTenants()) {
      semaphore.acquire();
        LocalDateTime localDateTime =  LocalDateTime.now();
        PublicationTenantRequestEntity ptrEntity = buildPublicationRequestEntity(publicationRequest, savedPublicationEntity, tenantId, localDateTime);

        var asyncHttpRequestFuture = executeAsyncHttpRequest(publicationRequest, tenantId, centralTenantContext)
          .whenComplete((resp, err) -> semaphore.release());

        var asd = savePublicationTenantRequest(asyncHttpRequestFuture, ptrEntity , centralTenantContext);

        futures.add(asd);
      }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
      .thenAccept(cf -> updatePublicationsStatus(futures, savedPublicationEntity));

    return buildPublicationResponse(savedPublicationEntity.getId().toString());
  }

  private CompletableFuture<Object> executeAsyncHttpRequest(PublicationRequest publicationRequest, String tenantId, FolioExecutionContext centralTenantContext) {
    return CompletableFuture.completedFuture(null)
      .thenApply(v -> {
        try (var context = new FolioExecutionContextSetter(prepareContextForTenant(tenantId, folioModuleMetadata, centralTenantContext))) {
          var response = httpRequestService.postRequest(publicationRequest.getUrl(), publicationRequest.getPayload());
          if (response.getStatusCode().is2xxSuccessful()) {
            log.info("executeAsyncTask:: successfully called {} on tenant {}", publicationRequest.getUrl(), tenantId);
            return response;
          } else {
            log.error("executeAsyncTask:: error making {} '{}' request on tenant {}", publicationRequest.getMethod(), publicationRequest.getUrl(), tenantId, new HttpException((String) response.getBody()));
            return CompletableFuture.failedFuture(new RuntimeException((String) response.getBody()));
          }
        }
      });
  }

  private CompletableFuture<PublicationTenantRequestEntity> savePublicationTenantRequest(CompletableFuture<Object> asyncHttpRequestFuture,
                                                                 PublicationTenantRequestEntity ptrEntity, FolioExecutionContext centralTenantContext) {
    try (var context = new FolioExecutionContextSetter(prepareContextForTenant(centralTenantContext.getTenantId(), folioModuleMetadata, centralTenantContext))) {
      return asyncHttpRequestFuture.handle((response, t) -> {
        if (t == null) {
          try {
            String responseAsString = objectMapper.writeValueAsString(response);
            ptrEntity.setStatus(PublicationResponse.StatusEnum.SUCCESS.toString());
            ptrEntity.setResponse(responseAsString);
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        } else {
          ptrEntity.setStatus(PublicationResponse.StatusEnum.ERROR.toString());
          ptrEntity.setResponse(t.getMessage());
        }
        return publicationTenantRequestRepository.save(ptrEntity);
      });
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  private PublicationTenantRequestEntity buildPublicationRequestEntity(PublicationRequest publicationRequest,
      PublicationStatusEntity savedPublicationEntity, String tenantId, LocalDateTime localDateTime) throws JsonProcessingException {

    PublicationTenantRequestEntity ptrEntity = new PublicationTenantRequestEntity();
    String payload = objectMapper.writeValueAsString(publicationRequest.getPayload());

    ptrEntity.setId(UUID.randomUUID());
    ptrEntity.setRequestUrl(publicationRequest.getUrl());
    ptrEntity.setRequestPayload(payload);
    ptrEntity.setStartedDate(localDateTime);
    ptrEntity.setTenantId(tenantId);
    ptrEntity.setPcId(savedPublicationEntity.getId());
    return ptrEntity;
  }

  private PublicationStatusEntity persistPublicationRecord(UUID userId, int totalRecords) {
    var publicationStatusEntity = new PublicationStatusEntity();
    publicationStatusEntity.setStatus(PublicationResponse.StatusEnum.IN_PROGRESS.toString());
    publicationStatusEntity.setTotalRecords(totalRecords);
    publicationStatusEntity.setUserId(userId);
    var savedPSE = publicationStatusRepository.save(publicationStatusEntity);

    log.info("Created publication record {}", savedPSE.getId());
    return savedPSE;
  }

  private void updatePublicationsStatus(List<CompletableFuture<PublicationTenantRequestEntity>> futures, PublicationStatusEntity savedPublication) {
    var isCompletedWithExceptions = futures.stream().anyMatch(CompletableFuture::isCompletedExceptionally);
    var updateStatus = isCompletedWithExceptions ? PublicationResponse.StatusEnum.ERROR : PublicationResponse.StatusEnum.SUCCESS;

    savedPublication.setStatus(updateStatus.toString());
    publicationStatusRepository.save(savedPublication);

    log.info("Updated publication record {} with status {}", savedPublication.getId(), savedPublication.getStatus());
  }

  private void validatePublicationRequest(UUID consortiumId, PublicationRequest publication, FolioExecutionContext context) {
    if (CollectionUtils.isEmpty(publication.getTenants())) {
      throw new PublicationException(TENANT_LIST_EMPTY);
    }
    tenantService.checkTenantsAndConsortiumExistsOrThrow(consortiumId, List.copyOf(publication.getTenants()));
    var userAffiliated = userTenantService.checkUserIfHasPrimaryAffiliationByUserId(consortiumId, context.getUserId().toString());
    if (!userAffiliated) {
      throw new PublicationException(PRIMARY_AFFILIATION_NOT_EXISTS);
    }
  }

  private PublicationResponse buildPublicationResponse(String publicationId) {
    return new PublicationResponse()
      .id(publicationId)
      .status(PublicationResponse.StatusEnum.IN_PROGRESS);
  }

}
