package org.folio.consortia.exception;

public class ResourceAlreadyExistException extends RuntimeException{
  private static final String RESOURCE_EXIST_MSG_TEMPLATE = "Object with %s [%s] is already presented in the system";
  private static final String CONSORTIUM_RESOURCE_EXIST_MSG_TEMPLATE = "System can not have more than one consortium record";

  public ResourceAlreadyExistException(String attribute, String value) {
    super(String.format(RESOURCE_EXIST_MSG_TEMPLATE, attribute, value));
  }

  public ResourceAlreadyExistException() {
    super(String.format(CONSORTIUM_RESOURCE_EXIST_MSG_TEMPLATE));
  }
}
