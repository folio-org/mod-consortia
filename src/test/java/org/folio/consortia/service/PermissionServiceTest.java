package org.folio.consortia.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import org.folio.consortia.client.PermissionsClient;
import org.folio.consortia.domain.dto.Permission;
import org.folio.consortia.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
class PermissionServiceTest {
  @InjectMocks
  PermissionServiceImpl permissionService;
  @Mock
  PermissionsClient permissionsClient;

  @Test
  void shouldCreatePermission() {
    Permission permission = new Permission();
    permission.setPermissionName("consortia.inventory.share.local.instance");
    permission.setDisplayName("Inventory: Share local instance with consortium");
    permission.setDescription("Inventory: Share local instance with consortium");

    doNothing().when(permissionsClient).createPermission(permission);

    permissionService.createPermission(permission);

    verify(permissionsClient).createPermission(permission);
  }
}
