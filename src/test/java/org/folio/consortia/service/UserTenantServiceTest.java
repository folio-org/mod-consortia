package org.folio.consortia.service;

import org.folio.consortia.domain.entity.Tenant;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.domain.repository.UserTenantRepository;
import org.folio.consortia.service.impl.UserTenantServiceImpl;
import org.folio.pv.domain.dto.UserTenantCollection;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
  public void shouldGetList() {
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
    assertEquals(limit, result.getTotalRecords());
  }

  @Test
  public void shouldFail() {
    // given
    UUID userId = UUID.randomUUID();
    String username = "testuser";
    when(userTenantRepository.findByUserId(userId)).thenReturn(new UserTenantEntity());
    when(userTenantRepository.findByUsername(username)).thenReturn(new UserTenantEntity());

    // when
    UserTenantCollection result = userTenantService.get(userId, username, null, null);

    // then
    assertEquals(1, result.getUserTenants().size());
    assertEquals(1, result.getTotalRecords());
  }
}

