package org.folio.consortia.domain.converter;

import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.entity.UserTenantEntity;

public class UserTenantConverter {

  public UserTenant toDto(UserTenantEntity source) {
    UserTenant userTenant = new UserTenant();
    userTenant.setId(source.getId());
    userTenant.setUserId(source.getUserId());
    userTenant.setUsername(source.getUsername());
    if (source.getTenant() != null) {
      userTenant.setTenantId(source.getTenant().getId());
    }
    userTenant.setIsPrimary(source.getIsPrimary());
    return userTenant;
  }
}
