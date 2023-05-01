package org.folio.consortia.utils;

import feign.FeignException;
import lombok.experimental.UtilityClass;
import org.folio.consortia.domain.dto.Error;
import org.folio.consortia.domain.dto.Errors;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ErrorHelper {

  public static Error createError(String message, ErrorType type, ErrorCode errorCode) {
    var error = new Error();
    error.setMessage(message);
    error.setType(type.getTypeCode());
    error.setCode(errorCode == null ? null : errorCode.name());
    return error;
  }


  public static Errors createErrors(Error error) {
    var e = new Errors();
    e.setErrors(List.of(error));
    return e;
  }

  public static Errors createUnknownError(String message) {
    return createErrors(createError(message, ErrorType.UNKNOWN, null));
  }

  public static Errors createInternalError(String message, ErrorCode errorCode) {
    return createErrors(createError(message, ErrorType.INTERNAL, errorCode));
  }

  public static Errors createExternalError(String message, ErrorCode errorCode) {
    return createErrors(createError(message, ErrorType.FOLIO_EXTERNAL_OR_UNDEFINED, errorCode));
  }

  public static Errors createPermissionError(FeignException e, ErrorCode errorCode){
    String message = e.getMessage();
    String extractedMessage = extractPermissionFromErrorMessage(message); // extract the main part from the error message
    return createErrors(createError(extractedMessage, ErrorType.INTERNAL, errorCode));
  }

  private String extractPermissionFromErrorMessage(String errorMessage) {
    String regex = "\\[.*: \\[(.*)\\]"; // regex to extract the main part from the error message
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(errorMessage);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return "unknown";
    }
  }

  public enum ErrorType {
    INTERNAL("-1"),
    FOLIO_EXTERNAL_OR_UNDEFINED("-2"),
    EXTERNAL_OR_UNDEFINED("-3"),
    UNKNOWN("-4");

    private final String typeCode;

    ErrorType(String typeCode) {
      this.typeCode = typeCode;
    }

    public String getTypeCode() {
      return typeCode;
    }

  }

  public enum ErrorCode {
    VALIDATION_ERROR, NOT_FOUND_ERROR, INTERACT_ERROR, DUPLICATE_ERROR, HAS_ACTIVE_USER_ASSOCIATION_ERROR, PERMISSION_ERROR
  }

}
