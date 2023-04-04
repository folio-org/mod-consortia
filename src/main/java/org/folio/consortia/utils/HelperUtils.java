package org.folio.consortia.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.folio.consortia.domain.dto.User;
import org.folio.spring.FolioExecutionContext;

public class HelperUtils {

  private HelperUtils() {}

  public static void checkIdenticalOrThrow(String firstString, String secondString, String errorMsg) {
    if (!StringUtils.equals(firstString, secondString)) {
      throw new IllegalArgumentException(errorMsg);
    }
  }

  public static boolean isUserActive(User user) {
    return user.getActive();
  }

  public static String getTenantId(FolioExecutionContext folioExecutionContext) {
    return folioExecutionContext.getTenantId();
  }

  public static String randomString(Integer noOfString) {
    RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
    return generator.generate(noOfString);
  }
}
