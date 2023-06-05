package org.folio.consortia.service.impl;

import static org.folio.consortia.utils.EntityUtils.createTenant;
import static org.folio.consortia.utils.EntityUtils.createTenantEntity;
import static org.folio.consortia.utils.EntityUtils.createUserTenantEntity;
import static org.folio.consortia.utils.InputOutputTestUtils.getMockData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.folio.consortia.config.kafka.KafkaService;
import org.folio.consortia.domain.dto.SyncPrimaryAffiliationBody;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.domain.dto.UserCollection;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserService;
import org.folio.consortia.service.UserTenantService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
class PrimaryAffiliationAsyncServiceImplTest {
  @InjectMocks
  PrimaryAffiliationAsyncServiceImpl primaryAffiliationAsyncService;
  @Mock
  UserService userService;
  @Mock
  TenantService tenantService;
  @Mock
  UserTenantRepository userTenantRepository;
  @Mock
  UserTenantService userTenantService;
  @Mock
  KafkaService kafkaService;
  @Autowired
  ObjectMapper objectMapper;

  @Test
  void createPrimaryUserAffiliations() throws JsonProcessingException {
    var consortiumId = UUID.randomUUID();
    var spab = new SyncPrimaryAffiliationBody();

    TenantEntity tenantEntity1 = createTenantEntity("ABC1", "TestName1");
    tenantEntity1.setConsortiumId(consortiumId);
    UserTenantEntity userTenantEntity = createUserTenantEntity(UUID.randomUUID());
    Tenant tenant = createTenant("TestID", "Test");

    var userCollectionString = getMockData("mockdata/user_collection.json");
    List<User> userCollection = new ObjectMapper().readValue(userCollectionString, UserCollection.class).getUsers();

    // stub collection of 2 users
    when(userService.getUsersByQuery(anyString(), anyInt(), anyInt())).thenReturn(userCollection);
    when(tenantService.getByTenantId(anyString())).thenReturn(tenantEntity1);
    when(userTenantRepository.findByUserIdAndTenantId(any(), anyString())).thenReturn(Optional.of(userTenantEntity));

    primaryAffiliationAsyncService.createPrimaryUserAffiliations(consortiumId, spab);
  }
}
