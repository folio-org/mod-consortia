package org.folio.consortia.domain.converter;

import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.pv.domain.dto.UserTenant;

public class UserTenantConverter {
  private UserTenantConverter() {
    throw new IllegalArgumentException("Faild to convert");
  }

  public static UserTenant toDto(UserTenantEntity userTenantEntity) {
    UserTenant userTenant = new UserTenant();
    userTenant.setId(userTenantEntity.getId());
    userTenant.setUserId(userTenantEntity.getUserId());
    userTenant.setUsername(userTenantEntity.getUsername());
    userTenant.setTenantId(userTenantEntity.getTenantId());
    userTenant.setIsPrimary(userTenantEntity.getIsPrimary());
    return userTenant;
  }
}
