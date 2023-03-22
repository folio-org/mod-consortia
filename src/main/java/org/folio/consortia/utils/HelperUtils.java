package org.folio.consortia.utils;

import org.apache.commons.lang3.StringUtils;

public class HelperUtils {

  private HelperUtils() {}

  public static void isIdentical(String firstString, String secondString)
  {
    if(!StringUtils.equals(firstString, secondString)) {
      throw new IllegalArgumentException("Arguments are not matching");
    }
  }
}
