package org.folio.consortia.exception;

public class ConsortiumClientException extends RuntimeException{

  public ConsortiumClientException(String errorMsg) {
    super(String.format(errorMsg));
  }
}
