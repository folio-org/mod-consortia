package org.folio.consortia.service.impl;

import static org.folio.consortia.utils.InputOutputTestUtils.getMockDataObject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.folio.consortia.domain.dto.PublicationRequest;
import org.folio.consortia.domain.entity.PublicationStatusEntity;
import org.folio.consortia.domain.entity.PublicationTenantRequestEntity;
import org.folio.consortia.repository.PublicationTenantRequestRepository;
import org.folio.consortia.service.HttpRequestService;
import org.folio.consortia.support.BaseUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class PublicationServiceImplTest extends BaseUnitTest {
  private static final String PUBLICATION_REQUEST_SAMPLE = "mockdata/publications/publication_request.json";
  private static final String PUBLICATION_STATUS_ENTITY_SAMPLE = "mockdata/publications/publication_status_entity.json";
  @InjectMocks
  private PublicationServiceImpl publicationService;
  @Mock
  PublicationTenantRequestRepository publicationTenantRequestRepository;
  @Mock
  HttpRequestService httpRequestService;
  @Mock
  ObjectMapper objectMapper;
  @Test
  void createTenantRequestEntitiesSuccess() throws JsonProcessingException {
    PublicationRequest pr = getMockDataObject(PUBLICATION_REQUEST_SAMPLE, PublicationRequest.class);
    var publicationStatusEntity = getMockDataObject(PUBLICATION_STATUS_ENTITY_SAMPLE, PublicationStatusEntity.class);
    publicationStatusEntity.setCreatedDate(LocalDateTime.now());

    when(objectMapper.writeValueAsString(anyString())).thenReturn(RandomStringUtils.random(10));
    when(publicationTenantRequestRepository.save(any(PublicationTenantRequestEntity.class))).thenReturn(new PublicationTenantRequestEntity());

    publicationService.processTenantRequests(pr, publicationStatusEntity);

    verify(publicationTenantRequestRepository, atLeast(pr.getTenants().size())).save(any());
  }

  @Test
  void executeAsyncHttpRequest() throws JsonProcessingException {
    var pr = getMockDataObject(PUBLICATION_REQUEST_SAMPLE, PublicationRequest.class);
    var payload = objectMapper.writeValueAsString(pr.getPayload());
    var publicationStatusEntity = getMockDataObject(PUBLICATION_STATUS_ENTITY_SAMPLE, PublicationStatusEntity.class);
    publicationStatusEntity.setCreatedDate(LocalDateTime.now());

    when(objectMapper.writeValueAsString(anyString())).thenReturn(RandomStringUtils.random(10));
    when(publicationTenantRequestRepository.save(any(PublicationTenantRequestEntity.class))).thenReturn(new PublicationTenantRequestEntity());

    ResponseEntity<String> restTemplateResponse = new ResponseEntity<>(payload, HttpStatusCode.valueOf(201));
    when(httpRequestService.performRequest(anyString(), eq(HttpMethod.POST), any())).thenReturn(restTemplateResponse);
    var response = publicationService.executeAsyncHttpRequest(pr, CENTRAL_TENANT_NAME, folioExecutionContext).join();
    Assertions.assertEquals(payload, response.getBody());
  }
}
