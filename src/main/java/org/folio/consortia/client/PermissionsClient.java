package org.folio.consortia.client;

import org.folio.consortia.domain.dto.Permission;
import org.folio.consortia.domain.dto.PermissionUser;
import org.folio.consortia.domain.dto.PermissionUserCollection;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("perms/users")
public interface PermissionsClient {
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  PermissionUserCollection get(@RequestParam("query") String query);

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  PermissionUserCollection get(@RequestParam("query") String query);

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  PermissionUser create(@RequestBody PermissionUser permissionUser);

  @PostMapping(value = "/{userId}/permissions?indexField=userId", consumes = MediaType.APPLICATION_JSON_VALUE)
  void addPermission(@PathVariable("userId") String userId, Permission permission);
}
