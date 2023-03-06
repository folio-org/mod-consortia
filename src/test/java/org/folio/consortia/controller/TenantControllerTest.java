package org.folio.consortia.controller;

import org.folio.consortia.support.BaseTest;
import org.folio.consortia.repository.entity.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EntityScan(basePackageClasses = Tenant.class)
class TenantControllerTest extends BaseTest {
  @Test
  void getTenants() throws Exception {
    var headers = defaultHeaders();
    this.mockMvc.perform(get("/consortia/tenants?query=tenantName==\"River2\"").headers(headers))
      .andExpect(
        matchAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON_VALUE)));
  }
}
