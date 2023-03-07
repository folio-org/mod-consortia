package org.folio.consortia.service;

import org.folio.pv.domain.dto.TenantCollection;
import org.folio.consortia.repository.TenantRepository;
import org.folio.consortia.repository.entity.Tenant;
import org.folio.consortia.service.impl.TenantServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
@EntityScan(basePackageClasses = Tenant.class)
class TenantServiceTest {
  @InjectMocks
  private TenantServiceImpl tenantService;
  @Mock
  private TenantRepository repository;

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

    TenantCollection tenantCollection = tenantService.get();
    Assertions.assertEquals(2, tenantCollection.getTotalRecords());
  }

}
