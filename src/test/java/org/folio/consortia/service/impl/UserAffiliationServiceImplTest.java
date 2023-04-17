package org.folio.consortia.service.impl;

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
  @Autowired
  KafkaTemplate kafkaTemplate;

  private static AutoCloseable mockitoMocks;
  @Mock
  TenantService tenantService;
  @Mock
  UserTenantService userTenantService;
  private final static org.folio.consortia.domain.dto.UserTenant userTenantSample = JsonTestUtils.readUserTenantMockFile("mockdata/kafka/primary_affiliation_request.json");;
  private RecordHeader createKafkaHeader(String headerName, String headerValue) {
    return new RecordHeader(headerName, headerValue.getBytes(StandardCharsets.UTF_8));
  }

  @BeforeEach
  public void beforeAll(){
    mockitoMocks = MockitoAnnotations.openMocks(this);
  }
  @AfterEach
  public void afterAll() throws Exception {
    mockitoMocks.close();
  }
  @Test
  void primaryAffiliationAddedSuccessfullyTest() {
    var te = new TenantEntity();
    te.setId(UUID.randomUUID().toString());
    te.setConsortiumId(UUID.randomUUID());
    doReturn(te).when(tenantService).getByTenantId(anyString());

    ProducerRecord<String, org.folio.consortia.domain.dto.UserTenant> record = new ProducerRecord<>("folio.diku.USER_CREATED", userTenantSample);
    record.headers()
      .add(createKafkaHeader(XOkapiHeaders.TENANT, TENANT))
      .add(createKafkaHeader(XOkapiHeaders.URL, wireMockServer.baseUrl()))
      .add(createKafkaHeader(XOkapiHeaders.TOKEN, TOKEN))
      .add(createKafkaHeader(XOkapiHeaders.USER_ID, UUID.randomUUID().toString()));

    kafkaTemplate.send(record);
    kafkaTemplate.flush();

    await()
      .atLeast(Duration.of(20, ChronoUnit.SECONDS))
      .atMost((Duration.of(50, ChronoUnit.SECONDS)))
      .with()
      .pollInterval(Duration.ofSeconds(1))
      .until(() -> false);
    log.info("ads");
  }

  @Test
  void tenantNotInConsortiaTest() {
    kafkaTemplate.send("folio.diku.USER_CREATED.topic", userTenantSample);

    log.info("done");
  }

  @Test
  void primaryAffiliationAlreadyExists() {
    kafkaTemplate.send("folio.diku.USER_CREATED.topic", userTenantSample);

    log.info("done");
  }

}
