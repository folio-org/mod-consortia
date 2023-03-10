package org.folio.consortia.service;

import org.folio.consortia.domain.entity.Tenant;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.domain.repository.UserTenantRepository;
import org.folio.consortia.exception.UserTenantNotFoundException;
import org.folio.consortia.service.impl.UserTenantServiceImpl;
import org.folio.pv.domain.dto.UserTenantCollection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
@EntityScan(basePackageClasses = Tenant.class)
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
    UserTenantCollection result = userTenantService.get(null, null, offset, limit);

    // then
    assertEquals(userTenantEntities.size(), result.getUserTenants().size());
    assertEquals(2, result.getTotalRecords());
  }

  @Test
  void shouldGetUserTenantListByUserId() {
    // given
    UUID userId = UUID.randomUUID();
    //create a userTenantEntity and set data
    UserTenantEntity userTenant = createUserTenantEntity(userId, "testuser");
    List<UserTenantEntity> userTenantEntities = List.of(userTenant);
    when(userTenantRepository.findByUserId(userId)).thenReturn(Optional.of(userTenantEntities.get(0)));

    // when
    UserTenantCollection result = userTenantService.get(userId, null, null, null);

    // then
    assertEquals(userTenantEntities.size(), result.getUserTenants().size());
    assertEquals(1, result.getTotalRecords());
  }

  @Test
  void shouldThrowIllegalArgumentException() {
    Assertions.assertThrows(IllegalArgumentException.class, () ->
      userTenantService.get(null, null, 0, 0));
  }

  @Test
  void shouldReturn404NotFoundException() {
    // given
    UUID userId = UUID.randomUUID();
    when(userTenantRepository.findByUserId(userId)).thenReturn(Optional.empty());

    // throw exception
    assertThrows(UserTenantNotFoundException.class, () -> userTenantService.get(userId, null, null, null));
  }

  private UserTenantEntity createUserTenantEntity(UUID userId, String username) {
    UserTenantEntity userTenantEntity = new UserTenantEntity();
    userTenantEntity.setUserId(userId);
    userTenantEntity.setTenant(new Tenant());
    userTenantEntity.setUsername(username);
    return userTenantEntity;
  }
}

