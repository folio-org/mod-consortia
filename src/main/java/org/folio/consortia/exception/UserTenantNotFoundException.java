package org.folio.consortia.exception;

import java.util.UUID;

public class UserTenantNotFoundException extends ResourceNotFoundException {

  protected static final String USER_TENANT_RESOURCE_NAME = "User Tenant";

  public UserTenantNotFoundException(String attribute, UUID id) {
    super(USER_TENANT_RESOURCE_NAME, attribute, id);
  }
}
