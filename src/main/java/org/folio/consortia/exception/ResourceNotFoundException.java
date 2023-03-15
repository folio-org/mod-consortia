package org.folio.consortia.exception;

public class ResourceNotFoundException extends RuntimeException {

  private static final String NOT_FOUND_MSG_TEMPLATE = "Object with %s [%s] was not found";

  public ResourceNotFoundException(String attribute, String value) {
    super(String.format(NOT_FOUND_MSG_TEMPLATE, attribute, value));
  }
}
