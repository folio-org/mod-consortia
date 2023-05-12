package org.folio.consortia.utils;

import org.folio.consortia.domain.dto.Payload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.util.Base64;

public class TokenUtils {

  public static Payload parseToken(String token) {
    String[] tokenParts = token.split("\\.");

    if(tokenParts.length == 3) {
      String encodedPayload = tokenParts[1];
      byte[] decodedJsonBytes = Base64.getDecoder().decode(encodedPayload);
      String decodedJson = new String(decodedJsonBytes);

      try {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper.readValue(decodedJson, Payload.class);
      } catch (Exception e) {
        return null;
      }
    } else {
      return null;
    }
  }

}
