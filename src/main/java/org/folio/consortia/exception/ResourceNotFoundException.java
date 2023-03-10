package org.folio.consortia.exception;

public abstract class ResourceNotFoundException extends RuntimeException {

  private static final String NOT_FOUND_MSG_TEMPLATE = "%s with %s [%s] was not found";

  protected ResourceNotFoundException(String resourceName, String attribute, String value) {
    super(String.format(NOT_FOUND_MSG_TEMPLATE, resourceName, attribute, value));
  }
}
