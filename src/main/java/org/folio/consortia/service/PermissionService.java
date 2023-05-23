package org.folio.consortia.service;

import org.folio.consortia.domain.dto.PermissionUser;

public interface PermissionService {

  /**
   * Adds permissions to permissionUser.
   *
   * @param permissionUser  the permissionUser
   * @param permissionsFilePath  the path of file includes permission names to add
   *
   * @return
   */
  void addPermissions(PermissionUser permissionUser, String permissionsFilePath);

  /**
   * Creates permissionUser for userId.
   *
   * @param userId  the id of user
   * @param permissionsFilePath  the path of file includes permission names to add
   *
   * @return PermissionUser
   */
  PermissionUser createPermissionUser(String userId, String permissionsFilePath);
}
