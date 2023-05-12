package org.folio.consortia.exception;

public class InvalidTokenException extends RuntimeException {
  private static final String TOKEN_NOT_FOUND = "Invalid token";

  public InvalidTokenException() {
    super(TOKEN_NOT_FOUND);
  }

}
