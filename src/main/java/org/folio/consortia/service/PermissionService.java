package org.folio.consortia.service;

import org.folio.consortia.domain.dto.Permission;

public interface PermissionService {

  /**
   * Create new permission
   * @param permission permission object
   */
  void createPermission(Permission permission);
}
