package org.folio.consortia.service;

import org.folio.consortia.domain.dto.PermissionUser;

public interface PermissionService {

  /**
   * Adds permissions to permissionUser.
   *
   * @param permissionUser  the permissionUser
   * @param fileName  the name of file
   *
   * @return
   */
  void addPermissions(PermissionUser permissionUser, String fileName);

  /**
   * Creates permissionUser for userId.
   *
   * @param userId  the id of user
   * @param fileName  the name of file
   *
   * @return PermissionUser
   */
  PermissionUser createPermissionUser(String userId, String fileName);
}
