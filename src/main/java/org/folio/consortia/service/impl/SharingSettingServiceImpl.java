package org.folio.consortia.service.impl;

import static org.folio.consortia.utils.HelperUtils.setSourceAsConsortium;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.folio.consortia.config.FolioExecutionContextHelper;
import org.folio.consortia.domain.dto.PublicationRequest;
import org.folio.consortia.domain.dto.SharingSettingRequest;
import org.folio.consortia.domain.dto.SharingSettingResponse;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.repository.SharingSettingRepository;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.PublicationService;
import org.folio.consortia.service.SharingSettingService;
import org.folio.consortia.service.TenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class SharingSettingServiceImpl implements SharingSettingService {

  private final SharingSettingRepository sharingSettingRepository;
  private final TenantService tenantService;
  private final ConsortiumService consortiumService;
  private final FolioExecutionContextHelper contextHelper;
  private final PublicationService publicationService;
  private final FolioExecutionContext folioExecutionContext;

  @Override
  public SharingSettingResponse start(UUID consortiumId, SharingSettingRequest sharingSettingRequest) {
    log.debug("start:: Trying to share setting with consortiumId: {}, sharing settingId: {}", consortiumId, sharingSettingRequest.getSettingId());
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    Set<String> tenantsAssociationWithSetting = sharingSettingRepository.findTenantsBySettingId(sharingSettingRequest.getSettingId());
    TenantCollection tenantCollection = tenantService.getAll(consortiumId);

    PublicationRequest publicationRequestPostMethod = createPublicationRequestForSetting(sharingSettingRequest, "POST");
    PublicationRequest publicationRequestPutMethod = createPublicationRequestForSetting(sharingSettingRequest, "PUT");

    // By traverse through all tenants in db,
    // we add tenant to put method publication tenant list, if it exists in setting tenant associations
    // otherwise, we add it to post method publication tenant list
    for (Tenant tenant : tenantCollection.getTenants()) {
      if (tenantsAssociationWithSetting.contains(tenant.getId())) {
        publicationRequestPutMethod.getTenants().add(tenant.getId());
      } else {
        publicationRequestPostMethod.getTenants().add(tenant.getId());
      }
    }
    log.info("start:: tenants with size: {} successfully added to appropriate publication request", tenantCollection.getTotalRecords());

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode payload = objectMapper.convertValue(sharingSettingRequest.getPayload(), JsonNode.class);
    var updatedPayload = setSourceAsConsortium(payload);
    publicationRequestPostMethod.setPayload(updatedPayload);
    publicationRequestPutMethod.setPayload(updatedPayload);
    log.info("start:: set source as a consortium");

    // we create PC request with POST and PUT Http method to create settings as a consortia-system-user
    try (var ignored = new FolioExecutionContextSetter(contextHelper.getSystemUserFolioExecutionContext(folioExecutionContext.getTenantId()))) {
      UUID createSettingsPcId = publishRequest(consortiumId, publicationRequestPostMethod);
      UUID updateSettingsPcId = publishRequest(consortiumId, publicationRequestPutMethod);
      return new SharingSettingResponse().createSettingsPCId(createSettingsPcId).updateSettingsPCId(updateSettingsPcId);
    }
  }

  private PublicationRequest createPublicationRequestForSetting(SharingSettingRequest sharingSettingRequest, String httpMethod) {
    PublicationRequest publicationRequest = new PublicationRequest();
    publicationRequest.setMethod(httpMethod);
    publicationRequest.setUrl(sharingSettingRequest.getUrl());
    publicationRequest.setPayload(sharingSettingRequest.getPayload());
    publicationRequest.setTenants(new HashSet<>());
    return publicationRequest;
  }

  private UUID publishRequest(UUID consortiumId, PublicationRequest publicationRequest) {
    if (!publicationRequest.getTenants().isEmpty()) {
      return publicationService.publishRequest(consortiumId, publicationRequest).getId();
    }
    log.info("publishRequest:: Tenant list of publishing for http method: {} is empty", publicationRequest.getMethod());
    return null;
  }
}
