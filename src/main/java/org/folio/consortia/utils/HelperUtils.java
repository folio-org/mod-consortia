package org.folio.consortia.utils;

import org.apache.commons.lang3.StringUtils;

public class HelperUtils {

  private HelperUtils() {}

  public static void isIdentical(String firstString, String secondString, String errorMsg)
  {
    if(!StringUtils.equals(firstString, secondString)) {
      throw new IllegalArgumentException(errorMsg);
    }
  }
}
