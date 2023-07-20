package org.folio.consortia.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.consortia.utils.EntityUtils.createPublicationRequestForSetting;
import static org.folio.consortia.utils.EntityUtils.createSharingSettingResponse;
import static org.folio.consortia.utils.EntityUtils.createTenant;
import static org.folio.consortia.utils.EntityUtils.createTenantCollection;
import static org.folio.consortia.utils.InputOutputTestUtils.getMockDataObject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.folio.consortia.config.FolioExecutionContextHelper;
import org.folio.consortia.domain.dto.PublicationResponse;
import org.folio.consortia.domain.dto.SharingSettingRequest;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.domain.entity.SharingSettingEntity;
import org.folio.consortia.repository.ConsortiumRepository;
import org.folio.consortia.repository.SharingSettingRepository;
import org.folio.consortia.service.impl.SharingSettingServiceImpl;
import org.folio.spring.FolioExecutionContext;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SharingSettingServiceTest {
  private static final String SHARING_SETTING_REQUEST_SAMPLE = "mockdata/sharing_setting_request.json";

  @InjectMocks
  private SharingSettingServiceImpl sharingSettingService;
  @Mock
  private ConsortiumRepository consortiumRepository;
  @Mock
  private ConsortiumService consortiumService;
  @Mock
  private TenantService tenantService;
  @Mock
  private PublicationService publicationService;
  @Mock
  private SharingSettingRepository sharingSettingRepository;
  @Mock
  private FolioExecutionContext folioExecutionContext;
  @Mock
  private FolioExecutionContextHelper contextHelper;

  @Test
  void shouldStartSharingSetting() {
    UUID consortiumId = UUID.randomUUID();
    UUID createSettingsPcId = UUID.randomUUID();
    UUID updateSettingsPcId = UUID.randomUUID();
    Tenant tenant1 = createTenant("tenant1", "tenant1");
    Tenant tenant2 = createTenant("tenant2", "tenant2");
    Set<String> tenantAssociationsWithSetting = Set.of("tenant1");
    TenantCollection tenantCollection = createTenantCollection(List.of(tenant1, tenant2));
    var sharingSettingRequest = getMockDataObject(SHARING_SETTING_REQUEST_SAMPLE, SharingSettingRequest.class);

    // "tenant1" exists in tenant setting association so that tenant1 is in PUT method publication,
    // "tenant2" is in POST method publication
    var publicationRequestPut = createPublicationRequestForSetting(sharingSettingRequest, "PUT", "CONSORTIUM-MARC");
    publicationRequestPut.setMethod("PUT");
    publicationRequestPut.setTenants(Set.of("tenant1"));
    var publicationRequestPost = createPublicationRequestForSetting(sharingSettingRequest, "POST", "CONSORTIUM-MARC");
    publicationRequestPost.setMethod("POST");
    publicationRequestPost.setTenants(Set.of("tenant2"));

    var publicationResponsePost = new PublicationResponse().id(createSettingsPcId);
    var publicationResponsePut = new PublicationResponse().id(updateSettingsPcId);

    when(consortiumRepository.existsById(consortiumId)).thenReturn(true);
    when(publicationService.publishRequest(consortiumId, publicationRequestPost)).thenReturn(publicationResponsePost);
    when(publicationService.publishRequest(consortiumId, publicationRequestPut)).thenReturn(publicationResponsePut);
    when(tenantService.getAll(consortiumId)).thenReturn(tenantCollection);
    when(sharingSettingRepository.findTenantsBySettingId(sharingSettingRequest.getSettingId())).thenReturn(tenantAssociationsWithSetting);
    when(sharingSettingRepository.save(any())).thenReturn(new SharingSettingEntity());
    when(folioExecutionContext.getTenantId()).thenReturn("mobius");
    doReturn(folioExecutionContext).when(contextHelper).getSystemUserFolioExecutionContext(anyString());

    var expectedResponse = createSharingSettingResponse(createSettingsPcId, updateSettingsPcId);
    var actualResponse = sharingSettingService.start(consortiumId, sharingSettingRequest);

    assertThat(actualResponse.getCreateSettingsPCId()).isEqualTo(expectedResponse.getCreateSettingsPCId());
    assertThat(actualResponse.getUpdateSettingsPCId()).isEqualTo(expectedResponse.getUpdateSettingsPCId());

    verify(publicationService, times(2)).publishRequest(any(), any());
  }
}
