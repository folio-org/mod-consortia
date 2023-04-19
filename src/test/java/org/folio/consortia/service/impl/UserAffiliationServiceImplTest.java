package org.folio.consortia.service.impl;

import static org.folio.consortia.utils.InputOutputTestUtils.getMockData;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.service.TenantService;
import org.folio.consortia.service.UserTenantService;
import org.folio.consortia.support.BaseTest;
import org.folio.consortia.utils.InputOutputTestUtils;
import org.folio.consortia.utils.JsonTestUtils;
import org.folio.spring.integration.XOkapiHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import lombok.extern.log4j.Log4j2;

@Log4j2
@ExtendWith(MockitoExtension.class)
class UserAffiliationServiceImplTest extends BaseTest {
  private static final String USER_CREATED_TOPIC = "FOLIO.Default.diku.USER_CREATED";
  @Autowired
  KafkaTemplate kafkaTemplate;

  private static AutoCloseable mockitoMocks;
  @Mock
  TenantService tenantService;
  @Mock
  UserTenantService userTenantService;
  private final static String userCreatedEventSample = getMockData("mockdata/kafka/primary_affiliation_request.json");;
  private RecordHeader createKafkaHeader(String headerName, String headerValue) {
    return new RecordHeader(headerName, headerValue.getBytes(StandardCharsets.UTF_8));
  }

  @BeforeEach
  public void beforeEach() {
    mockitoMocks = MockitoAnnotations.openMocks(this);
  }
  @AfterEach
  public void afterEach() throws Exception {
    mockitoMocks.close();
  }
  @Test
  void primaryAffiliationAddedSuccessfullyTest() {
    var te = new TenantEntity();
    te.setId(UUID.randomUUID().toString());
    te.setConsortiumId(UUID.randomUUID());
//    doReturn(te).when(tenantService).getByTenantId(anyString());

    ProducerRecord<String, String> record = new ProducerRecord<>(USER_CREATED_TOPIC, userCreatedEventSample);
    setDefaultHeaders(record);

/*
    kafkaTemplate.send(record);
    kafkaTemplate.flush();
*/

 /*   await()
      .atLeast(Duration.of(3, ChronoUnit.SECONDS))
      .atMost((Duration.of(10, ChronoUnit.SECONDS)))
      .with()
      .pollInterval(Duration.ofSeconds(1))
      .until(() -> false);*/
    log.info("ads");
  }



  @Test
  void tenantNotInConsortiaTest() {
  //  kafkaTemplate.send("folio.Default.diku.USER_CREATED", userCreatedEventSample);

    log.info("done");
  }

  @Test
  void primaryAffiliationAlreadyExists() {
//    kafkaTemplate.send("folio.diku.USER_CREATED", userCreatedEventSample);

    log.info("done");
  }

  private void setDefaultHeaders(ProducerRecord<String, String> record) {
    record.headers()
      .add(createKafkaHeader(XOkapiHeaders.TENANT, TENANT))
      .add(createKafkaHeader(XOkapiHeaders.URL, wireMockServer.baseUrl()))
      .add(createKafkaHeader(XOkapiHeaders.TOKEN, TOKEN))
      .add(createKafkaHeader(XOkapiHeaders.USER_ID, UUID.randomUUID().toString()));
  }
}
