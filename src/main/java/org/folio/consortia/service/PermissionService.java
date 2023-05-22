package org.folio.consortia.service;

import org.folio.consortia.domain.dto.Permission;
import org.folio.consortia.domain.dto.PermissionUser;

import java.util.List;
import java.util.Optional;

public interface PermissionService {

  /**
  * Gets permissionUser based on userId.
  *
  * @param userId  the id of user
  *
  * @return PermissionUser
  */
  Optional<PermissionUser> getPermissionUserById(String userId);

  /**
  * Creates permissionUser with given permissions.
  *
  * @param id the id of permissionUser
  * @param userId the id of user
  * @param permissionList list of permissions
  *
  * @return PermissionUser
  */
  PermissionUser createPermissionUserWithPermissions(String id, String userId, List<String> permissionList);

  /**
  * Creates permissionUser with given permissions.
  *
  * @param id the id of permissionUser
  * @param permission permission
  *
  * @return PermissionUser
  */
  void addPermissionToUser(String id, Permission permission);
}
