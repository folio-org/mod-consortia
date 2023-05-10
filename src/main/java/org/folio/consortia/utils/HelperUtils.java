package org.folio.consortia.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.folio.consortia.domain.entity.UserTenantEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HelperUtils {

  private HelperUtils() {}

  public static void checkIdenticalOrThrow(String firstString, String secondString, String errorMsg) {
    if (!StringUtils.equals(firstString, secondString)) {
      throw new IllegalArgumentException(errorMsg);
    }
  }

  public static String randomString(Integer noOfString) {
    RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
    return generator.generate(noOfString);
  }

  public static StringBuilder createQuery(List<UserTenantEntity> userTenantEntityList) {
    StringBuilder builder = new StringBuilder();
    List<String> userIds = new ArrayList<>(userTenantEntityList.stream().map(userTenantEntity -> userTenantEntity.getUserId().toString()).toList());
    Iterator<String> iterator = userIds.iterator();
    while (iterator.hasNext()){
      String userId = iterator.next();
      if (userIds.size() == 1) {
       builder.append("(id="+ userId +")");
      } else {
        builder.append("(id=" + userId + ")OR");
        iterator.remove();
      }
    }
    return builder;
  }
}
