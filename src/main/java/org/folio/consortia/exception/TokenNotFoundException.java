package org.folio.consortia.exception;

public class TokenNotFoundException extends RuntimeException {
  private static final String TOKEN_NOT_FOUND = "Token not found";

  public TokenNotFoundException() {
    super(TOKEN_NOT_FOUND);
  }

}
