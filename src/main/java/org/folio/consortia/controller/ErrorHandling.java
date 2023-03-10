package org.folio.consortia.controller;

import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.tenant.domain.dto.Errors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.folio.consortia.utils.ErrorHelper.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.consortia.utils.ErrorHelper.createInternalError;

//@RestControllerAdvice
public class ErrorHandling {

//  @ResponseStatus(HttpStatus.NOT_FOUND)
//  @ExceptionHandler(ResourceNotFoundException.class)
//  public Errors handleNotFoundException(ResourceNotFoundException e) {
//    return createInternalError(e.getMessage(), NOT_FOUND_ERROR);
//  }
}
