package org.folio.consortia.service.impl;

import com.google.common.io.Resources;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.folio.consortia.client.PermissionsClient;
import org.folio.consortia.domain.dto.Permission;
import org.folio.consortia.domain.dto.PermissionUser;
import org.folio.consortia.service.PermissionUserService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class PermissionUserServiceImpl implements PermissionUserService {

  private final PermissionsClient permissionsClient;

  @Override
  public Optional<PermissionUser> getByUserId(String userId) {
    return permissionsClient.get("userId==" + userId)
      .getPermissionUsers()
      .stream()
      .findFirst();
  }

  @Override
  public PermissionUser createWithEmptyPermissions(String id, String userId, List<String> permissionList) {
    var permissionUser = PermissionUser.of(UUID.randomUUID().toString(), userId, permissionList);
    log.info("Creating permissionUser {}.", permissionUser);
    return permissionsClient.create(permissionUser);
  }

  @Override
  public void addPermissions(PermissionUser permissionUser, String permissionsFilePath) {
    var permissions = readPermissionsFromResource(permissionsFilePath);
    if (CollectionUtils.isEmpty(permissions)) {
      throw new IllegalStateException("No user permissions found in " + permissionsFilePath);
    }
    // remove duplicate permissions already existing in permission user.
    permissions.removeAll(permissionUser.getPermissions());
    permissions.forEach(permission -> {
      var p = new Permission();
      p.setPermissionName(permission);
      try {
        log.info("Adding to user {} permission {}.", permissionUser.getUserId(), p);
        permissionsClient.addPermission(permissionUser.getUserId(), p);
      } catch (Exception e) {
        log.error("Error adding permission: {} to userId: {}.", permission, permissionUser.getUserId(), e);
        throw e;
      }
    });
  }

  @Override
  public PermissionUser createPermissionUser(String userId, String permissionsFilePath) {
    List<String> perms = readPermissionsFromResource(permissionsFilePath);

    if (CollectionUtils.isEmpty(perms)) {
      throw new IllegalStateException("No user permissions found in " + permissionsFilePath);
    }
    var permissionUser = PermissionUser.of(UUID.randomUUID().toString(), userId, perms);
    log.info("Creating permissionUser {}.", permissionUser);
    return permissionsClient.create(permissionUser);
  }

  private List<String> readPermissionsFromResource(String permissionsFilePath) {
    List<String> result = new ArrayList<>();
    var url = Resources.getResource(permissionsFilePath);

    try {
      result = Resources.readLines(url, StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error("Can't read user permissions from {}.", permissionsFilePath, e);
    }
    return result;
  }
}
