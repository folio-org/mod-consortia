package org.folio.consortia.controller;

import org.folio.consortia.support.BaseTest;
import org.folio.repository.entity.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EntityScan(basePackageClasses = Tenant.class)
class HealthCheckControllerTest extends BaseTest {

  @Test
  void healthCheckGet() throws Exception {
    var headers = defaultHeaders();
    this.mockMvc.perform(get("/consortia/health-check").headers(headers))
      .andExpect(status().isOk());
  }
}
