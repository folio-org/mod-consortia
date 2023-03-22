package org.folio.consortia.utils;

import org.apache.commons.lang3.StringUtils;

public class HelperUtils {

  private HelperUtils() {}

  public static void isIdentical(String first, String second)
  {
    if(!StringUtils.equals(first, second)) {
      throw new IllegalArgumentException("Arguments are not matching");
    }
  }
}
