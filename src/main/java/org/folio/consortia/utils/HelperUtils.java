package org.folio.consortia.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class HelperUtils {

  private static final String FOLIO_SOURCE_VALUE = "folio";
  private static final String CONSORTIUM_FOLIO = "CONSORTIUM-FOLIO";
  private static final String CONSORTIUM_MARK = "CONSORTIUM-MARC";


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

  public static ObjectNode setSourceAsConsortium(JsonNode payload) {
    String source = FOLIO_SOURCE_VALUE.equalsIgnoreCase(payload.get("source").asText()) ? CONSORTIUM_FOLIO: CONSORTIUM_MARK;
    return ((ObjectNode) payload).put("source", source);
  }
}
