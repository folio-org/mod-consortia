package org.folio.consortia.messaging.domain;

import java.util.UUID;

import lombok.Data;

@Data
public class QmCompletedEventPayload {

  private UUID recordId;
  private boolean isSucceed;
  private String errorMessage;
}
