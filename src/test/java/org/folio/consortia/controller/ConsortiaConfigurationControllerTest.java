package org.folio.consortia.controller;

import static org.folio.consortia.utils.EntityUtils.createConsortiaConfigurationEntity;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.folio.consortia.domain.entity.ConsortiaConfigurationEntity;
import org.folio.consortia.repository.ConsortiaConfigurationRepository;
import org.folio.consortia.support.BaseIT;
import org.folio.spring.integration.XOkapiHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class ConsortiaConfigurationControllerTest extends BaseIT {

  @MockitoBean
  ConsortiaConfigurationRepository configurationRepository;

  @Test
  void shouldGetConsortiumConfiguration() throws Exception {
    var header = defaultHeaders();
    header.set(XOkapiHeaders.TENANT, "testtenat1");

    when(configurationRepository.findAll())
      .thenReturn(List.of(createConsortiaConfigurationEntity("diku")));

    this.mockMvc.perform(
        get("/consortia-configuration")
          .headers(header))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.centralTenantId", is("diku")));
  }

  @ParameterizedTest
  @ValueSource(strings = {"{\"centralTenantId\":\"diku\"}"})
  void shouldSaveConsortiumConfiguration(String contentString) throws Exception {
    var header = defaultHeaders();
    ConsortiaConfigurationEntity configuration = new ConsortiaConfigurationEntity();
    configuration.setId(UUID.randomUUID());
    configuration.setCentralTenantId("diku");

    when(configurationRepository.count()).thenReturn(0L);
    when(configurationRepository.save(any(ConsortiaConfigurationEntity.class))).thenReturn(configuration);

    this.mockMvc.perform(
        post("/consortia-configuration")
          .headers(header).content(contentString))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.centralTenantId", is("diku")));
  }

}
