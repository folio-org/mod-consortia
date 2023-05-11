package org.folio.consortia.client;

import org.folio.consortia.domain.entity.PermissionUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("perms/users")
public interface PermissionsClient {

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  PermissionUser create(@RequestBody PermissionUser permissionUser);
}
