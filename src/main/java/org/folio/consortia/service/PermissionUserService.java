package org.folio.consortia.service;

import org.folio.consortia.domain.dto.Permission;
import org.folio.consortia.domain.dto.PermissionUser;

import java.util.List;
import java.util.Optional;

public interface PermissionUserService {

  /**
  * Gets permissionUser based on userId.
  *
  * @param userId  the id of user
  *
  * @return PermissionUser
  */
  Optional<PermissionUser> getByUserId(String userId);

  /**
  * Creates permissionUser with given permissions.
  *
  * @param id the id of permissionUser
  * @param userId the id of user
  * @param permissionList list of permissions
  *
  * @return PermissionUser
  */
  PermissionUser createWithPermissions(String id, String userId, List<String> permissionList);

  /**
  * Add permissions for existed permission user.
  *
  * @param id the id of permissionUser
  * @param permission permission
  */
  void addPermissionToUser(String id, Permission permission);
}
