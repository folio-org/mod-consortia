package org.folio.consortia.controller;

import static org.awaitility.Awaitility.await;
import static org.folio.consortia.utils.EntityUtils.createPublicationTenantRequestEntity;
import static org.folio.consortia.utils.InputOutputTestUtils.getMockDataAsString;
import static org.folio.consortia.utils.InputOutputTestUtils.getMockDataObject;
import static org.folio.consortia.utils.InputOutputTestUtils.writeValueAsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.folio.consortia.domain.dto.PublicationRequest;
import org.folio.consortia.domain.dto.PublicationStatus;
import org.folio.consortia.domain.entity.PublicationStatusEntity;
import org.folio.consortia.domain.entity.PublicationTenantRequestEntity;
import org.folio.consortia.repository.PublicationStatusRepository;
import org.folio.consortia.repository.PublicationTenantRequestRepository;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.HttpRequestService;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserTenantService;
import org.folio.consortia.support.BaseIT;
import org.folio.spring.integration.XOkapiHeaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import one.util.streamex.StreamEx;

public class PublicationControllerTest extends BaseIT {
  public static final String PUBLICATIONS_URL = "/consortia/%s/publications";
  public static final String GET_PUBLICATION_BY_ID_URL = "/consortia/%s/publications/%s";
  public static final String GET_PUBLICATION_RESULTS_BY_ID_URL = "/consortia/%s/publications/%s/results";
  @MockBean
  TenantService tenantService;
  @MockBean
  UserTenantService userTenantService;
  @Autowired
  HttpRequestService httpRequestService;
  @MockBean
  RestTemplate restTemplate;
  @MockBean
  PublicationStatusRepository publicationStatusRepository;
  @MockBean
  PublicationTenantRequestRepository publicationTenantRequestRepository;
  @MockBean
  ConsortiumService consortiumService;

  @Test
  void publicationSuccessful() throws Exception {
    var headers = defaultHeaders();
    var consortiumId = UUID.randomUUID();
    var publicationString = getMockDataAsString("mockdata/publications/publication_request.json");
    var publicationStatusEntity = getMockDataObject("mockdata/publications/publication_status_entity.json", PublicationStatusEntity.class);
    publicationStatusEntity.setId(UUID.randomUUID());

    doNothing().when(tenantService).checkTenantsAndConsortiumExistsOrThrow(any(UUID.class), any());
    when(userTenantService.checkUserIfHasPrimaryAffiliationByUserId(any(UUID.class), any())).thenReturn(true);
    when(publicationStatusRepository.save(any())).thenReturn(publicationStatusEntity);

    this.mockMvc.perform(post(String.format(PUBLICATIONS_URL, consortiumId)).headers(headers)
      .content(publicationString))
      .andExpectAll(status().is2xxSuccessful());
  }

  @Test
  void publicationWithTenantException() throws Exception {
    var headers = defaultHeaders();
    var consortiumId = UUID.randomUUID();
    var publicationString = getMockDataAsString("mockdata/publications/publication_request.json");
    var publicationStatusEntity = getMockDataObject("mockdata/publications/publication_status_entity.json", PublicationStatusEntity.class);
    publicationStatusEntity.setId(UUID.randomUUID());

    var respEntity = new ResponseEntity<>(publicationString, HttpStatusCode.valueOf(400));

    doNothing().when(tenantService).checkTenantsAndConsortiumExistsOrThrow(any(UUID.class), any());
    when(userTenantService.checkUserIfHasPrimaryAffiliationByUserId(any(UUID.class), any())).thenReturn(true);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(respEntity);
    when(publicationStatusRepository.save(any())).thenReturn(publicationStatusEntity);

    this.mockMvc.perform(post(String.format(PUBLICATIONS_URL, consortiumId)).headers(headers)
        .content(publicationString))
      .andExpectAll(status().is2xxSuccessful());
  }
  @Test
  void publicationPreValidationError() throws Exception {
    var headers = defaultHeaders();
    var consortiumId = UUID.randomUUID();
    var publicationString = getMockDataAsString("mockdata/publications/publication_request.json");

    doNothing().when(tenantService).checkTenantsAndConsortiumExistsOrThrow(any(UUID.class), any());
    when(userTenantService.checkUserIfHasPrimaryAffiliationByUserId(any(UUID.class), any())).thenReturn(false);

    this.mockMvc.perform(post(String.format(PUBLICATIONS_URL, consortiumId)).headers(headers)
        .content(publicationString))
      .andExpectAll(status().is4xxClientError());
  }

  @Test
  void getPublicationSuccessful() throws Exception {
    var headers = defaultHeaders();
    var consortiumId = UUID.randomUUID();
    var publicationId = UUID.randomUUID();

    var publicationStatusEntity = getMockDataObject("mockdata/publications/publication_status_entity.json", PublicationStatusEntity.class);
    publicationStatusEntity.setStatus(PublicationStatus.ERROR);
    publicationStatusEntity.setCreatedDate(LocalDateTime.now());

    doNothing().when(consortiumService).checkConsortiumExistsOrThrow(any(UUID.class));
    when(publicationStatusRepository.findById(publicationId)).thenReturn(Optional.of(publicationStatusEntity));

    var tenantRequest1 = createPublicationTenantRequestEntity(publicationStatusEntity, TENANT, PublicationStatus.COMPLETE, 201);
    var tenantRequest2 = createPublicationTenantRequestEntity(publicationStatusEntity, TENANT, PublicationStatus.ERROR, 400);
    List<PublicationTenantRequestEntity> ptrEntityMockResponse = List.of(tenantRequest1, tenantRequest2);

    Page<PublicationTenantRequestEntity> ptrEntities  = new PageImpl<>(ptrEntityMockResponse);
    when(publicationTenantRequestRepository.findByPcStateId(eq(publicationId), any())).thenReturn(ptrEntities);

    this.mockMvc.perform(get(String.format(GET_PUBLICATION_BY_ID_URL, consortiumId, publicationId)).headers(headers))
      .andExpectAll(
        status().is2xxSuccessful(),
        jsonPath("$.status", is(PublicationStatus.ERROR.getValue())),
        jsonPath("$.errors", is(not(empty())))
      );
  }

  @Test
  void getPublicationResultsSuccessful() throws Exception {
    var headers = defaultHeaders();
    var consortiumId = UUID.randomUUID();
    var publicationId = UUID.randomUUID();

    var publicationStatusEntity = getMockDataObject("mockdata/publications/publication_status_entity.json", PublicationStatusEntity.class);
    publicationStatusEntity.setStatus(PublicationStatus.ERROR);
    publicationStatusEntity.setCreatedDate(LocalDateTime.now());

    doNothing().when(consortiumService).checkConsortiumExistsOrThrow(any(UUID.class));
    when(publicationStatusRepository.findById(publicationId)).thenReturn(Optional.of(publicationStatusEntity));

    var tenantRequest1 = createPublicationTenantRequestEntity(publicationStatusEntity, TENANT, PublicationStatus.COMPLETE, 201);
    var tenantRequest2 = createPublicationTenantRequestEntity(publicationStatusEntity, TENANT, PublicationStatus.ERROR, 400);
    List<PublicationTenantRequestEntity> ptrEntityMockResponse = List.of(tenantRequest1, tenantRequest2);

    Page<PublicationTenantRequestEntity> ptrEntities  = new PageImpl<>(ptrEntityMockResponse);
    when(publicationTenantRequestRepository.findByPcStateId(eq(publicationId), any())).thenReturn(ptrEntities);

    this.mockMvc.perform(get(String.format(GET_PUBLICATION_RESULTS_BY_ID_URL, consortiumId, publicationId)).headers(headers))
      .andExpectAll(
        status().is2xxSuccessful(),
        jsonPath("$.publicationResults", is(not(empty()))),
        jsonPath("$.totalRecords", is(ptrEntityMockResponse.size()))
      );
  }

  @ParameterizedTest
  @CsvSource(value = {"100, 12"})
  void parallelTenantRequestsTest(int tenantsAmount, int chunkSize) throws InterruptedException, JsonProcessingException {
    var headers = defaultHeaders();
    var consortiumId = UUID.randomUUID();
    var publicationString = getMockDataAsString("mockdata/publications/publication_request.json");
    var publicationStatusEntity = getMockDataObject("mockdata/publications/publication_status_entity.json",
        PublicationStatusEntity.class);
    publicationStatusEntity.setId(UUID.randomUUID());

    var ptre = createPublicationTenantRequestEntity(publicationStatusEntity, TENANT, PublicationStatus.IN_PROGRESS, 200);
    ptre.setStatus(PublicationStatus.IN_PROGRESS);
    ptre.setId(UUID.randomUUID());

    var ptreAsString = new ObjectMapper().writeValueAsString(ptre);

    doNothing().when(tenantService)
      .checkTenantsAndConsortiumExistsOrThrow(any(UUID.class), any());
    when(userTenantService.checkUserIfHasPrimaryAffiliationByUserId(any(UUID.class), any())).thenReturn(true);
    when(publicationStatusRepository.save(any())).thenReturn(publicationStatusEntity);
    when(publicationTenantRequestRepository.save(any())).thenReturn(ptre);

    // prepare a list of different tenants for making publish coordinator tenant requests
    var listOfTenantNames = IntStream.rangeClosed(1, tenantsAmount)
      .boxed()
      .map(val -> "tenant_" + val)
      .toList();

    ResponseEntity<String> restTemplateResponse = new ResponseEntity<>(ptreAsString, HttpStatusCode.valueOf(201));

    ArgumentCaptor<HttpEntity<Object>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), entityCaptor.capture(), eq(String.class))).thenReturn(restTemplateResponse);

    // split list of tenants into chunks and make parallel API calls
    StreamEx.ofSubLists(listOfTenantNames, chunkSize)
      .parallel()
      .forEach(tenants -> {
        PublicationRequest publicationRequest = getMockDataObject("mockdata/publications/publication_request.json", PublicationRequest.class);
        publicationRequest.setTenants(new HashSet<>(tenants));
        var requestBody = writeValueAsString(publicationRequest);

        try {
          this.mockMvc.perform(post(String.format(PUBLICATIONS_URL, consortiumId))
            .headers(headers)
            .content(requestBody))
            .andExpectAll(status().is2xxSuccessful());

        } catch (Exception e) {
          Assertions.fail(e);
        }
      });

    await().until(() -> entityCaptor.getAllValues().size() == tenantsAmount);

    Assertions.assertEquals(tenantsAmount, entityCaptor.getAllValues().size());
    // build a list of 'x-okapi-tenant' values
    var capturedTenantHeaders = entityCaptor.getAllValues()
      .stream()
      .map(a -> a.getHeaders().get(XOkapiHeaders.TENANT).iterator().next())
      .distinct()
      .sorted()
      .toList();

    verify(restTemplate, times(listOfTenantNames.size())).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
    // check if all 'x-okapi-tenant' values match with the initial list of expected tenant names
    Assertions.assertTrue(CollectionUtils.isEqualCollection(capturedTenantHeaders, listOfTenantNames));
  }
}
