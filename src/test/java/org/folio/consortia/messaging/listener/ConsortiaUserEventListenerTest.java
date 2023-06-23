package org.folio.consortia.messaging.listener;

import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.service.UserAffiliationService;
import org.folio.consortia.support.BaseTest;
import static org.folio.consortia.utils.InputOutputTestUtils.getMockData;
import org.folio.spring.integration.XOkapiHeaders;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;

public class ConsortiaUserEventListenerTest extends BaseTest {
  private static final String CONSORTIUM_TENANT_ID = "diku";
  private static final String STANDALONE_TENANT_ID = "some_tenant";

  private static final String userCreatedEventSample = getMockData("mockdata/kafka/create_primary_affiliation_request.json");
  private static final String userDeletedEventSample = getMockData("mockdata/kafka/delete_primary_affiliation_request.json");

  @Autowired
  private ConsortiaUserEventListener eventListener;
  @Autowired
  private UserAffiliationService userAffiliationService;

  @Test
  public void userCreatedEventCanCallConsortiaConfigurationTable() {
    // in this test tenant included into consortium, table consortia_configuration exists and can be queried
    // consortia_configuration has no rows, so ResourceNotFoundException is expected
    assertThrows(ResourceNotFoundException.class, () -> eventListener.listenUserCreated(userCreatedEventSample, getMessageHeaders(CONSORTIUM_TENANT_ID)));
  }

  @Test
  public void userDeletedEventCanCallConsortiaConfigurationTable() {
    // in this test tenant included into consortium, table consortia_configuration exists and can be queried
    // consortia_configuration has no rows, so ResourceNotFoundException is expected
    assertThrows(ResourceNotFoundException.class, () -> eventListener.listenUserDeleted(userDeletedEventSample, getMessageHeaders(CONSORTIUM_TENANT_ID)));
  }

  @Test
  public void userCreatedEventCompletesWithoutProcessingFor() {
    // in this test tenant does not include into consortium, so table consortia_configuration does not exist
    // offset committed without event processing, situation is possible when consortium and standalone tenants exists on the same cluster
    eventListener.listenUserCreated(userCreatedEventSample, getMessageHeaders(STANDALONE_TENANT_ID));
  }

  @Test
  public void userDeletedEventCompletesWithoutProcessingFor() {
    // in this test tenant does not include into consortium, so table consortia_configuration does not exist
    // offset committed without event processing, situation is possible when consortium and standalone tenants exists on the same cluster
    eventListener.listenUserCreated(userCreatedEventSample, getMessageHeaders(STANDALONE_TENANT_ID));
  }

  private MessageHeaders getMessageHeaders(String tenantId) {
    Map<String, Object> header = new HashMap<>();
    header.put(XOkapiHeaders.TENANT, tenantId.getBytes());

    return new MessageHeaders(header);
  }
}
