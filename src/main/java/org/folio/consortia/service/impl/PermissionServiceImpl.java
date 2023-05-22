package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.client.PermissionsClient;
import org.folio.consortia.domain.dto.Permission;
import org.folio.consortia.domain.dto.PermissionUser;
import org.folio.consortia.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

  private final PermissionsClient permissionsClient;

  @Override
  public Optional<PermissionUser> getPermissionUserById(String userId) {
    return permissionsClient.get("userId==" + userId)
      .getPermissionUsers()
      .stream()
      .findFirst();
  }

  @Override
  public PermissionUser createPermissionUserWithPermissions(String id, String userId, List<String> permissionList) {
    var permissionUser = PermissionUser.of(UUID.randomUUID().toString(), userId, permissionList);
    log.info("Creating permissionUser {}.", permissionUser);
    return permissionsClient.create(permissionUser);
  }

  @Override
  public void addPermissionToUser(String id, Permission permission) {
    permissionsClient.addPermission(id, permission);
  }
}
