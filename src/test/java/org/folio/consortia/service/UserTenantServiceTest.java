package org.folio.consortia.service;

import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.domain.repository.UserTenantRepository;
import org.folio.consortia.exception.UserTenantNotFoundException;
import org.folio.consortia.service.impl.UserTenantServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
@EntityScan(basePackageClasses = UserTenantEntity.class)
@RunWith(SpringRunner.class)
class UserTenantServiceTest {

  @Mock
  private UserTenantRepository userTenantRepository;

  @InjectMocks
  private UserTenantServiceImpl userTenantService;

  @Test
  void shouldGetUserTenantList() {
    // given
    int offset = 0;
    int limit = 10;
    List<UserTenantEntity> userTenantEntities = List.of(new UserTenantEntity(), new UserTenantEntity());
    Page<UserTenantEntity> userTenantPage = new PageImpl<>(userTenantEntities, PageRequest.of(offset, limit), userTenantEntities.size());
    when(userTenantRepository.findAll(PageRequest.of(offset, limit))).thenReturn(userTenantPage);

    // when
    var result = userTenantService.get(offset, limit);

    // then
    assertEquals(userTenantEntities.size(), result.getUserTenants().size());
    assertEquals(2, result.getTotalRecords());
  }

  @Test
  void shouldGetUserTenantByAssociationId() {
    // given
    UUID associationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    UserTenantEntity userTenant = createUserTenantEntity(associationId, userId, "testuser");
    List<UserTenantEntity> userTenantEntities = List.of(userTenant);
    when(userTenantRepository.findById(associationId)).thenReturn(Optional.of(userTenantEntities.get(0)));

    // when
    var result = userTenantService.getById(associationId);

    // then
    assertEquals(associationId, result.getId());
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void shouldGetUserTenantListByUserId() {
    // given
    UUID userId = UUID.randomUUID();
    UUID associationId = UUID.randomUUID();

    UserTenantEntity userTenant = createUserTenantEntity(associationId, userId, "testuser");
    UserTenantEntity userTenant2 = createUserTenantEntity(associationId, userId, "testuser");
    List<UserTenantEntity> userTenantEntities = List.of(userTenant);
    when(userTenantRepository.findByUserId(userId)).thenReturn(Optional.of(userTenantEntities.get(0)));

    // when
    UserTenantCollection result = userTenantService.getByUserId(userId);

    // then
    assertEquals(userTenant2, userTenant);
    assertEquals(userTenantEntities.size(), result.getUserTenants().size());
    assertEquals(1, result.getTotalRecords());
  }

  @Test
  void shouldGetUserTenantByUsernameAndTenantId() {
    // given
    UUID userId = UUID.randomUUID();
    UUID associationId = UUID.randomUUID();
    String tenantId = String.valueOf(UUID.randomUUID());

    UserTenantEntity userTenant = createUserTenantEntity(associationId, userId, "testuser");
    List<UserTenantEntity> userTenantEntities = List.of(userTenant);
    when(userTenantRepository.findByUsernameAndTenantId("testuser", tenantId)).thenReturn(userTenantEntities);

    // when
    UserTenantCollection result = userTenantService.getByUsername("testuser", tenantId);

    // then
    assertEquals(userTenantEntities.size(), result.getUserTenants().size());
    assertEquals(1, result.getTotalRecords());
  }

  @Test
  void shouldThrowIllegalArgumentException() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> userTenantService.get(0, 0));
  }

  @Test
  void shouldReturn404UserIdNotFoundException() {
    // given
    UUID userId = UUID.randomUUID();
    when(userTenantRepository.findByUserId(userId)).thenReturn(Optional.empty());

    // throw exception
    assertThrows(UserTenantNotFoundException.class, () -> userTenantService.getByUserId(userId));
  }

  @Test
  void shouldReturn404UsernameNotFoundException() {
    // given
    String username = "testuser";
    when(userTenantRepository.findByUsername(username)).thenReturn(new ArrayList<>());

    // throw exception
    assertThrows(UserTenantNotFoundException.class, () -> userTenantService.getByUsername("testusername", null));
  }

  private UserTenantEntity createUserTenantEntity(UUID associationId, UUID userId, String username) {
    UserTenantEntity userTenantEntity = new UserTenantEntity();
    userTenantEntity.setId(associationId);
    userTenantEntity.setUserId(userId);
    userTenantEntity.setTenant(new TenantEntity());
    userTenantEntity.setUsername(username);
    return userTenantEntity;
  }
}

