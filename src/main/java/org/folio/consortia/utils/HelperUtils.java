package org.folio.consortia.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class HelperUtils {

  private static final String CONSORTIUM_FOLIO = "CONSORTIUM-FOLIO";
  private static final String CONSORTIUM_MARC = "CONSORTIUM-MARC";
  private static final String CONSORTIUM_WITH_LOWERCASE = "consortium";

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

  /**
   * This method as common method for both setting and instance sharing process
   * For setting, source will set always 'consortium'
   * For instance, if source is folio, then it will set as a CONSORTIUM-FOLIO
   *               if source is marc, then it will set as a CONSORTIUM-MARC.
   *
   * @param payload payload of requested object
   * @param isForSetting whether set source for setting or not
   * @return updated payload
   */
  public static ObjectNode setSourceAsConsortium(JsonNode payload, boolean isForSetting) {
    String payloadSource = payload.get("source").asText();
    String updatedSource;
    if (isForSetting) updatedSource = CONSORTIUM_WITH_LOWERCASE;
    else updatedSource = switch (payloadSource) {
      case "folio" -> CONSORTIUM_FOLIO;
      case "marc" -> CONSORTIUM_MARC;
      default -> throw new IllegalStateException("source is not recognized");
    };
    return ((ObjectNode) payload).put("source", updatedSource);
  }
}
