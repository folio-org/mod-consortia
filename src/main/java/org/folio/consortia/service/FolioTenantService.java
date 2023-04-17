package org.folio.consortia.service;

import java.sql.ResultSet;

import org.apache.commons.lang3.BooleanUtils;
import org.folio.consortia.messaging.topic.KafkaTopicsInitializer;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Primary
public class FolioTenantService extends TenantService {

  private static final String EXIST_SQL =
    "SELECT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?)";

  private final KafkaTopicsInitializer kafkaTopicsInitializer;

  public FolioTenantService(JdbcTemplate jdbcTemplate,
    KafkaTopicsInitializer kafkaTopicsInitializer,
                            FolioExecutionContext context,
                            FolioSpringLiquibase folioSpringLiquibase) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.kafkaTopicsInitializer = kafkaTopicsInitializer;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    try {
      kafkaTopicsInitializer.createTopics();
      kafkaTopicsInitializer.restartEventListeners();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Implemented by HSQLDB way
   * Check if the tenant exists (by way of its database schema)
   * @return if the tenant's database schema exists
   */
  @Override
  protected boolean tenantExists() {
    return BooleanUtils.isTrue(
      jdbcTemplate.query(EXIST_SQL,
        (ResultSet resultSet) -> resultSet.next() && resultSet.getBoolean(1),
        getSchemaName()
      )
    );
  }
}
