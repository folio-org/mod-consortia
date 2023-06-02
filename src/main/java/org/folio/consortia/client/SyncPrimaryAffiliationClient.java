package org.folio.consortia.client;

import org.folio.consortia.domain.dto.SyncPrimaryAffiliationBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "consortia")
public interface SyncPrimaryAffiliationClient {
  @PostMapping(value = "/{consortiumId}/tenants/{tenantId}/sync-primary-affiliations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  SyncPrimaryAffiliationBody syncPrimaryAffiliations(@PathVariable("consortiumId") String consortiumId,
      @PathVariable("tenantId") String tenantId);

  @PostMapping(value = "/{consortiumId}/tenants/{tenantId}/primary-affiliation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  SyncPrimaryAffiliationBody primaryAffiliation(@RequestBody SyncPrimaryAffiliationBody syncPrimaryAffiliationBody,
      @PathVariable("consortiumId") String consortiumId, @PathVariable("tenantId") String tenantId);

}
