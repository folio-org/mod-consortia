package org.folio.consortia.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.folio.consortia.client.SyncPrimaryAffiliationClient;
import org.folio.consortia.domain.dto.SyncPrimaryAffiliationBody;
import org.folio.consortia.domain.dto.SyncUser;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.service.SyncPrimaryAffiliationService;
import org.folio.consortia.service.UserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@AllArgsConstructor
public class SyncPrimaryAffiliationServiceImpl implements SyncPrimaryAffiliationService {
  private final UserService userService;
  private final SyncPrimaryAffiliationClient syncPrimaryAffiliationClient;

  @Override
  @Async
  public void syncPrimaryAffiliations(UUID consortiumId, String tenantId) {
    log.info("Start creating user primary affiliation for tenant {}", tenantId);
    List<User> users = new ArrayList<>();
    try {
      users = userService.getUsersByQuery("cql.allRecords=1", 0, Integer.MAX_VALUE);
    } catch (Exception e) {
      log.error("syncPrimaryAffiliations:: failed to retrieve users list");
    }
    if (CollectionUtils.isNotEmpty(users)) {
      SyncPrimaryAffiliationBody spab = buildSyncPrimaryAffiliationBody(tenantId, users);
      syncPrimaryAffiliationClient.primaryAffiliation(spab, consortiumId.toString(), tenantId);
    }
  }

  private SyncPrimaryAffiliationBody buildSyncPrimaryAffiliationBody(String tenantId, List<User> users) {
    var syncUsers = users.stream()
      .map(user -> new SyncUser().id(user.getId())
        .username(user.getUsername()))
      .toList();
    return new SyncPrimaryAffiliationBody().tenantId(tenantId)
      .users(syncUsers);

  }
}
