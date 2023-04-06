package org.folio.consortia.exception;

import java.util.UUID;

public class ConsortiumClientException extends RuntimeException {
  private static final String CLIENT_ERROR_MSG_TEMPLATE = "Could not get user with id %s";

  public ConsortiumClientException(UUID id, Exception exception) {
    super(String.format(CLIENT_ERROR_MSG_TEMPLATE, id), exception);
  }
}
