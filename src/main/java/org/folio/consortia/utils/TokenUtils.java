package org.folio.consortia.utils;

import org.apache.commons.lang3.StringUtils;
import org.folio.consortia.domain.dto.Payload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.util.Base64;

public class TokenUtils {

  private TokenUtils() {}

  public static boolean isValid(String token) {
    if (StringUtils.isBlank(token)) {
      return false;
    }

    String[] tokenParts = token.split("\\.");

    if (tokenParts.length != 3) {
      return false;
    }

    String encodedPayload = tokenParts[1];
    byte[] decodedJsonBytes = Base64.getDecoder().decode(encodedPayload);
    String decodedJson = new String(decodedJsonBytes);

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      Payload payload = objectMapper.readValue(decodedJson, Payload.class);

      return !payload.getSub().contains("UNDEFINED_USER__");
    } catch (Exception e) {
      return false;
    }
  }

}
