package org.folio.consortia.controller;

import org.folio.consortia.domain.entity.ConsortiumEntity;
import org.folio.consortia.domain.repository.ConsortiumRepository;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.support.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConsortiumControllerTest extends BaseTest {
  @MockBean
  ConsortiumRepository consortiumRepository;

  @ParameterizedTest
  @ValueSource(strings = {
    "{\"id\":\"111841e3-e6fb-4191-8fd8-5674a5107c33\",\"name\":\"consortium_name\"}"
  })
  void shouldGet4xxErrorWhileSaving(String contentString) throws Exception {
    var headers = defaultHeaders();
    when(consortiumRepository.findAll()).thenThrow(ResourceAlreadyExistException.class);
    this.mockMvc.perform(
        post("/consortia")
          .headers(headers)
          .content(contentString))
        .andExpect(matchAll(status().is4xxClientError(),
          jsonPath("$.errors[0].code", is("RESOURCE_ALREADY_EXIST"))));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "{\"id\":\"111841e3-e6fb-4191-8fd8-5674a5107c33\",\"name\":\"consortium_name\"}"
  })
  void shouldSaveConsortium(String contentString) throws Exception {
    var headers = defaultHeaders();
    ConsortiumEntity consortiumEntity = createConsortiumEntity("111841e3-e6fb-4191-8fd8-5674a5107c33", "Test");
    when(consortiumRepository.save(any(ConsortiumEntity.class))).thenReturn(consortiumEntity);

    this.mockMvc.perform(
        post("/consortia")
          .headers(headers)
          .content(contentString))
      .andExpect(matchAll(status().isOk()));
  }

  private ConsortiumEntity createConsortiumEntity(String id, String name) {
    ConsortiumEntity consortiumEntity = new ConsortiumEntity();
    consortiumEntity.setId(UUID.fromString(id));
    consortiumEntity.setName(name);
    return consortiumEntity;
  }
}
