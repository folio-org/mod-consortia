package org.folio.consortia.service.impl;

import org.folio.consortia.client.PermissionsClient;
import org.folio.consortia.domain.dto.Permission;
import org.folio.consortia.service.PermissionService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

  private final PermissionsClient permissionsClient;

  @Override
  public void createPermission(Permission permission) {
    permissionsClient.createPermission(permission);
  }
}
