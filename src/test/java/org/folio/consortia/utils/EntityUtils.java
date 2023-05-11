package org.folio.consortia.utils;

import lombok.experimental.UtilityClass;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.entity.ConsortiumEntity;
import org.folio.consortia.domain.entity.TenantEntity;

import java.util.UUID;

@UtilityClass
public class EntityUtils {

  public static TenantEntity createTenantEntity(String id, String name, String code, Boolean isCentral) {
    TenantEntity tenantEntity = new TenantEntity();
    tenantEntity.setId(id);
    tenantEntity.setCode(code);
    tenantEntity.setName(name);
    tenantEntity.setIsCentral(isCentral);
    return tenantEntity;
  }

  public static TenantEntity createTenantEntity() {
    TenantEntity tenantEntity = new TenantEntity();
    tenantEntity.setId("testtenant1");
    tenantEntity.setCode("ABC");
    tenantEntity.setName("testtenant1");
    tenantEntity.setIsCentral(false);
    return tenantEntity;
  }

  public TenantEntity createTenantEntity(String id, String name) {
    TenantEntity tenantEntity = new TenantEntity();
    tenantEntity.setId(id);
    tenantEntity.setCode("ABC");
    tenantEntity.setName(name);
    tenantEntity.setIsCentral(false);
    return tenantEntity;
  }

  public Tenant createTenant(String id, String name) {
    Tenant tenant = new Tenant();
    tenant.setId(id);
    tenant.setName(name);
    tenant.setIsCentral(false);
    tenant.setCode("ABC");
    return tenant;
  }

  public ConsortiumEntity createConsortiumEntity(String id, String name) {
    ConsortiumEntity consortiumEntity = new ConsortiumEntity();
    consortiumEntity.setId(UUID.fromString(id));
    consortiumEntity.setName(name);
    return consortiumEntity;
  }
}
