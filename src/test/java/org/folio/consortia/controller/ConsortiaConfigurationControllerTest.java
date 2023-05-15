package org.folio.consortia.controller;

import org.folio.consortia.domain.entity.ConsortiaConfigurationEntity;
import org.folio.consortia.repository.ConsortiaConfigurationRepository;
import org.folio.consortia.support.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConsortiaConfigurationControllerTest extends BaseTest {

  @MockBean
  ConsortiaConfigurationRepository configurationRepository;

  @ParameterizedTest
  @ValueSource(strings = {"{\"id\":\"111841e3-e6fb-4191-8fd8-5674a5107c33\",\"centralTenantId\":\"diku\"}"})
  void shouldSaveConsortiumConfiguration(String contentString) throws Exception {
    var headers = defaultHeaders();
    ConsortiaConfigurationEntity configuration = new ConsortiaConfigurationEntity();
    configuration.setId(UUID.randomUUID());
    configuration.setCentralTenantId("diku");

    when(configurationRepository.count()).thenReturn(0L);
    when(configurationRepository.save(any(ConsortiaConfigurationEntity.class))).thenReturn(configuration);

    this.mockMvc.perform(
      post("/consortia_configuration")
        .headers(headers).content(contentString))
      .andExpect(status().isCreated());
  }
//  void shouldSaveConsortiumConfiguration(String contentString) throws Exception {
//    var headers = defaultHeaders();
//    ConsortiumEntity consortiumEntity = createConsortiumEntity("111841e3-e6fb-4191-8fd8-5674a5107c33", "Test");
//
//    when(configurationRepository.count()).thenReturn(0L);
//
//    this.mockMvc.perform(
//        post("/consortia")
//          .headers(headers)
//          .content(contentString))
//      .andExpectAll(status().is2xxSuccessful(),
//        jsonPath("$.id", is("111841e3-e6fb-4191-8fd8-5674a5107c33")),
//        jsonPath("$.centralTenantId", is("central_tenant_id")));
//  }
}
