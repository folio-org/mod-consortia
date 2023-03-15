package org.folio.consortia.controller;

import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.support.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EntityScan(basePackageClasses = TenantEntity.class)
class TenantControllerTest extends BaseTest {

  @Test
  void getTenants() throws Exception {
    var headers = defaultHeaders();
    this.mockMvc.perform(get("/consortia/tenants?limit=2&offset=1").headers(headers))
      .andExpectAll(
        status().isOk(),
        content().contentType(MediaType.APPLICATION_JSON_VALUE));
  }

  @Test
  void getBadRequest() throws Exception {
    var headers = defaultHeaders();
    this.mockMvc.perform(get("/consortia/tenants?limit=0&offset=0").headers(headers))
      .andExpectAll(
        status().is4xxClientError(),
        content().contentType(MediaType.APPLICATION_JSON_VALUE),
        jsonPath("$.errors[0].message", is("Limit cannot be negative or zero: 0")));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "{\"id\":\"diku\",\"name\":\"diku_tenant_name\"}"
  })
  void saveTenant(String contentString) throws Exception {
    var headers = defaultHeaders();
    this.mockMvc.perform(
      post("/consortia/tenants")
      .headers(headers)
      .content(contentString))
      .andExpect(
        matchAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON_VALUE)));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "{\"name\":\"diku_tenant_name\"}"
  })
  void ShouldGet4xxErrorWhileSaving(String contentString) throws Exception {
    var headers = defaultHeaders();
    this.mockMvc.perform(
        post("/consortia/tenants")
          .headers(headers)
          .content(contentString))
      .andExpect(
        matchAll(
          status().is4xxClientError()));
  }
}
