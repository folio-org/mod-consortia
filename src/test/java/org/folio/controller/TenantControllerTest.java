package org.folio.controller;

import org.folio.support.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:tenant.sql")
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete.sql")
public class TenantControllerTest extends BaseTest {
  @Test
  @DisplayName("Find all tenants")
  void getTenants() throws Exception {
    mockMvc
      .perform(
        get("/consortia/tenants")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .headers(defaultHeaders()))
      .andExpect(
        matchAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON_VALUE),
          jsonPath("$.totalRecords", is(6)),
          jsonPath("$.tenants", hasSize(6))));
  }
}
