package org.folio.consortia.exception;

public class UserTenantNotFoundException extends ResourceNotFoundException {

  protected static final String USER_TENANT_RESOURCE_NAME = "User Tenant";

  public UserTenantNotFoundException(String attribute, String value) {
    super(USER_TENANT_RESOURCE_NAME, attribute, value);
  }
}
