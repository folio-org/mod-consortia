package org.folio.consortia.service.impl;

import static org.folio.consortia.utils.EntityUtils.createTenant;
import static org.folio.consortia.utils.EntityUtils.createTenantEntity;
import static org.folio.consortia.utils.InputOutputTestUtils.getMockData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.folio.consortia.config.kafka.KafkaService;
import org.folio.consortia.domain.dto.SyncPrimaryAffiliationBody;
import org.folio.consortia.domain.dto.SyncUser;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.domain.dto.UserCollection;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.repository.TenantRepository;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserService;
import org.folio.consortia.service.UserTenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
@EntityScan(basePackageClasses = UserTenantEntity.class)
class PrimaryAffiliationAsyncServiceImplTest {
  @InjectMocks
  PrimaryAffiliationAsyncServiceImpl primaryAffiliationAsyncService;
  @Mock
  private UserTenantService userTenantService;
  @Mock
  private TenantService tenantService;
  @Mock
  private KafkaService kafkaService;
  @Mock
  private UserTenantRepository userTenantRepository;
  @Mock
  private FolioModuleMetadata folioModuleMetadata;
  @Mock
  private FolioExecutionContext folioExecutionContext;

  @Mock
  UserService userService;
  @Mock
  TenantRepository tenantRepository;

  protected static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWt1X2FkbWluIiwidXNlcl9pZCI6IjFkM2I1OGNiLTA3YjUtNWZjZC04YTJhLTNjZTA2YTBlYjkwZiIsImlhdCI6MTYxNjQyMDM5MywidGVuYW50IjoiZGlrdSJ9.2nvEYQBbJP1PewEgxixBWLHSX_eELiBEBpjufWiJZRs";
  protected static final String TENANT = "diku";

  @Test
  void createPrimaryUserAffiliations() throws JsonProcessingException {
    var consortiumId = UUID.randomUUID();
    var tenantId = "ABC1";
    TenantEntity tenantEntity1 = createTenantEntity(tenantId, "TestName1");
    tenantEntity1.setConsortiumId(consortiumId);

    Tenant tenant = createTenant("TestID", "Test");

    var userCollectionString = getMockData("mockdata/user_collection.json");
    List<User> userCollection = new ObjectMapper().readValue(userCollectionString, UserCollection.class).getUsers();

    var syncUser = new SyncUser().id(UUID.randomUUID()
        .toString())
      .username("test_user");
    var spab = new SyncPrimaryAffiliationBody()
      .users(Collections.singletonList(syncUser))
      .tenantId(tenantId);

    // stub collection of 2 users
    when(tenantService.getCentralTenantId()).thenReturn(tenant.getId());
    when(tenantService.getByTenantId(anyString())).thenReturn(tenantEntity1);
    when(tenantRepository.findById(anyString())).thenReturn(Optional.of(tenantEntity1));
    when(userService.getUsersByQuery(anyString(), anyInt(), anyInt())).thenReturn(userCollection);
    when(userTenantService.createPrimaryUserTenantAffiliation(any(), any(), anyString(), anyString())).thenReturn(new UserTenant());

    when(folioExecutionContext.getTenantId()).thenReturn(tenantId);
    when(folioExecutionContext.getInstance()).thenReturn(folioExecutionContext);
    Map<String, Collection<String>> okapiHeaders = new HashMap<>();
    okapiHeaders.put(XOkapiHeaders.TENANT, List.of(tenantId));
    when(folioExecutionContext.getOkapiHeaders()).thenReturn(okapiHeaders);

    primaryAffiliationAsyncService.createPrimaryUserAffiliations(consortiumId, spab);

    verify(kafkaService, timeout(2000)).send(any(), anyString(), anyString());
  }
}