package org.folio.consortia.controller;

import org.folio.consortia.support.BaseTest;
import org.folio.repository.entity.Tenant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;


@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:tenant.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete.sql")
@EntityScan(basePackageClasses = Tenant.class)
class TenantControllerTest extends BaseTest {

  @Test
  @DisplayName("Find all tenants")
  void getTenantsList() throws Exception {
    this.mockMvc
      .perform(
        get("/consortia/tenants")
          .headers(defaultHeaders()))
      .andExpect(
        matchAll(
          jsonPath("$.totalRecords", is(6)),
          jsonPath("$.tenants", hasSize(6))));
  }
}
