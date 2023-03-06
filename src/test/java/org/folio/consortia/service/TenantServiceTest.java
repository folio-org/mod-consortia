package org.folio.consortia.service;

import org.folio.pv.domain.dto.TenantCollection;
import org.folio.repository.CQLService;
import org.folio.repository.TenantRepository;
import org.folio.repository.entity.Tenant;
import org.folio.service.impl.TenantServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@EntityScan(basePackageClasses = Tenant.class)
class TenantServiceTest {
  @InjectMocks
  private TenantServiceImpl tenantService;
  @Mock
  private TenantRepository repository;

  @Mock
  CQLService cqlService;

  @Test
  void shouldGetTenantList()
  {
    Tenant tenant1 = new Tenant();
    tenant1.setTenantId("ABC1");
    tenant1.setTenantName("TestName1");

    Tenant tenant2 = new Tenant();
    tenant1.setTenantId("ABC2");
    tenant1.setTenantName("TestName2");
    List<Tenant> tenantList = new ArrayList<>();
    tenantList.add(tenant1);
    tenantList.add(tenant2);
    Mockito.when(repository.findAll()).thenReturn(tenantList);

    TenantCollection tenantCollection = tenantService.get(null, 0,0);
    Assertions.assertEquals(2, tenantCollection.getTotalRecords());

  }

  @Test
  void shouldGetTenantListByQuery()
  {
    Tenant tenant1 = new Tenant();
    tenant1.setTenantId("ABC1");
    tenant1.setTenantName("TestName1");

    Tenant tenant2 = new Tenant();
    tenant1.setTenantId("ABC2");
    tenant1.setTenantName("TestName2");
    List<Tenant> tenantList = new ArrayList<>();
    tenantList.add(tenant1);
    tenantList.add(tenant2);
    Mockito.when(repository.findAll()).thenReturn(tenantList);
    String query = "query=tenantName==\"Test\"";
    TenantCollection tenantCollection = tenantService.get(query, 0,0);
    Assertions.assertEquals(0, tenantCollection.getTotalRecords());

  }
}
