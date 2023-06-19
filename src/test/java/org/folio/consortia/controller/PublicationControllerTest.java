package org.folio.consortia.controller;

import static org.folio.consortia.utils.InputOutputTestUtils.getMockData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.folio.consortia.client.UserTenantsClient;
import org.folio.consortia.domain.dto.PublicationRequest;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserTenantService;
import org.folio.consortia.support.BaseTest;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.mock.mockito.MockBean;

public class PublicationControllerTest extends BaseTest {
  public static final String PRIMARY_AFFILIATIONS_URL = "/consortia/%s/publications";
  @MockBean
  TenantService tenantService;
  @MockBean
  UserTenantService userTenantService;

  @Test
  void publicationSuccessful() throws Exception {
    var headers = defaultHeaders();
    var consortiumId = UUID.randomUUID();
    var publicationString = getMockData("mockdata/publication_request.json");

    PublicationRequest publicationRequest = new ObjectMapper().readValue(publicationString, PublicationRequest.class);

    doNothing().when(tenantService).checkTenantsAndConsortiumExistsOrThrow(any(UUID.class), any());
    when(userTenantService.checkUserIfHasPrimaryAffiliationByUserId(any(UUID.class), any())).thenReturn(true);

    this.mockMvc.perform(post(String.format(PRIMARY_AFFILIATIONS_URL, consortiumId)).headers(headers)
      .content(publicationString))
      .andExpectAll(status().is2xxSuccessful());
  }

  @Test
  void publicationWithTenantException() throws Exception {
    var headers = defaultHeaders();
    var consortiumId = UUID.randomUUID();
    var publicationString = getMockData("mockdata/publication_request.json");

    doNothing().when(tenantService).checkTenantsAndConsortiumExistsOrThrow(any(UUID.class), any());
    when(userTenantService.checkUserIfHasPrimaryAffiliationByUserId(any(UUID.class), any())).thenReturn(true);

    this.mockMvc.perform(post(String.format(PRIMARY_AFFILIATIONS_URL, consortiumId)).headers(headers)
        .content(publicationString))
      .andExpectAll(status().is2xxSuccessful());

    //check for async errors
  }
  @Test
  void publicationPreValidationError() throws Exception {
    var headers = defaultHeaders();
    var consortiumId = UUID.randomUUID();
    var publicationString = getMockData("mockdata/publication_request.json");

    doNothing().when(tenantService).checkTenantsAndConsortiumExistsOrThrow(any(UUID.class), any());
    when(userTenantService.checkUserIfHasPrimaryAffiliationByUserId(any(UUID.class), any())).thenReturn(false);

    this.mockMvc.perform(post(String.format(PRIMARY_AFFILIATIONS_URL, consortiumId)).headers(headers)
        .content(publicationString))
      .andExpectAll(status().is4xxClientError());
  }

  /*
   * private String getMockPublicationRequest() throws JsonProcessingException { var publication =
   * getMockData("mockdata/publication_request.json"); return new ObjectMapper().readValue(publication, PublicationRequest.class); }
   */
}
