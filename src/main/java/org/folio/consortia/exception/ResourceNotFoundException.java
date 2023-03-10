package org.folio.consortia.exception;

import java.util.UUID;

public abstract class ResourceNotFoundException extends RuntimeException {

  private static final String NOT_FOUND_MSG_TEMPLATE = "%s with %s [%s] was not found";

  protected ResourceNotFoundException(String resourceName, String attribute, UUID id) {
    super(String.format(NOT_FOUND_MSG_TEMPLATE, attribute, resourceName, id));
  }
}
