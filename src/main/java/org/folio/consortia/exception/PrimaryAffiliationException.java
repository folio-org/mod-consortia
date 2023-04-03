package org.folio.consortia.exception;

public class PrimaryAffiliationException extends RuntimeException{
  private static final String PRIMARY_AFFILIATION_NOT_EXIST_MSG_TEMPLATE = "Primary affiliation does not exist in the system for %s [%s]";

  public PrimaryAffiliationException(String attribute, String value) {
    super(String.format(PRIMARY_AFFILIATION_NOT_EXIST_MSG_TEMPLATE, attribute, value));
  }
}
