package org.folio.consortia.controller;

import static org.folio.consortia.utils.EntityUtils.createSharingInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.folio.consortia.domain.dto.SharingInstance;
import org.folio.consortia.repository.ConsortiumRepository;
import org.folio.consortia.service.SharingInstanceService;
import org.folio.consortia.support.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

class SharingInstanceControllerTest extends BaseTest {

  @MockBean
  private SharingInstanceService sharingInstanceService;
  @MockBean
  private ConsortiumRepository consortiumRepository;

  @ParameterizedTest
  @ValueSource(strings = {
    "{\"instanceIdentifier\":\"111841e3-e6fb-4191-8fd8-5674a5107c33\",\"sourceTenantId\":\"college\", \"targetTenantId\":\"mobius\"}"
  })
  void shouldSaveSharingInstance(String body) throws Exception {
    SharingInstance sharingInstance = createSharingInstance(
      UUID.fromString("111841e3-e6fb-4191-8fd8-5674a5107c33"), "college", "mobius");
    var headers = defaultHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    when(consortiumRepository.existsById(UUID.fromString("7698e46-c3e3-11ed-afa1-0242ac120002"))).thenReturn(true);
    when(sharingInstanceService.save(any(), any())).thenReturn(sharingInstance);

    this.mockMvc.perform(post("/consortia/7698e46-c3e3-11ed-afa1-0242ac120002/sharing/instances")
        .headers(headers)
        .content(body)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.instanceIdentifier").value("111841e3-e6fb-4191-8fd8-5674a5107c33"))
      .andExpect(jsonPath("$.sourceTenantId").value("college"))
      .andExpect(jsonPath("$.targetTenantId").value("mobius"));
  }

}
