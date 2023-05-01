package org.folio.consortia.exception;

public class PrimaryAffiliationException extends RuntimeException {
  private static final String USER_HAS_PRIMARY_AFFILIATION_WITH_TENANT = "User with id [%s] has primary affiliation with tenant [%s]";

  public PrimaryAffiliationException(String userId, String tenantId) {
    super(String.format(USER_HAS_PRIMARY_AFFILIATION_WITH_TENANT, userId, tenantId));
  }

  public PrimaryAffiliationException(String errorMsg) {
    super(String.format(errorMsg));
  }
}
