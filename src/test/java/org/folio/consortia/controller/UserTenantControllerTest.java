package org.folio.consortia.controller;

import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.service.UserTenantService;
import org.folio.consortia.support.BaseTest;
import org.folio.pv.domain.dto.UserTenant;
import org.folio.pv.domain.dto.UserTenantCollection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EntityScan(basePackageClasses = UserTenantEntity.class)
class UserTenantControllerTest extends BaseTest {

  @Mock
  private UserTenantService userTenantService;
  @InjectMocks
  private UserTenantController userTenantController;

  @Test
  void shouldGetUserTenants() {
    // given
    UUID userId = UUID.randomUUID();
    int offset = 0;
    int limit = 10;

    List<UserTenant> userTenantDtos = List.of(new UserTenant(), new UserTenant());
    UserTenantCollection userTenantCollection = new UserTenantCollection();
    userTenantCollection.setUserTenants(userTenantDtos);
    userTenantCollection.setTotalRecords(userTenantDtos.size());

    when(userTenantService.get(userId, null, offset, limit))
      .thenReturn(userTenantCollection);

    // when
    ResponseEntity<UserTenantCollection> response = userTenantController.getUserTenants(userId, null, offset, limit);

    // then
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertEquals(userTenantCollection, response.getBody());

    verify(userTenantService).get(userId, null, offset, limit);
  }

  @Test
  void shouldGetUserTenantList() throws Exception {
    var headers = defaultHeaders();
    this.mockMvc.perform(get("/consortia/user-tenants?limit=2&offset=1").headers(headers))
      .andExpectAll(
        status().isOk(),
        content().contentType(MediaType.APPLICATION_JSON_VALUE));
  }

  @Test
  void getBadRequest() throws Exception {
    var headers = defaultHeaders();
    this.mockMvc.perform(get("/consortia/user-tenants?limit=0&offset=0").headers(headers))
      .andExpectAll(
        status().is4xxClientError(),
        content().contentType(MediaType.TEXT_PLAIN + ";charset=UTF-8"),
        jsonPath("$", is("Page size must not be less than one")));
  }

}
