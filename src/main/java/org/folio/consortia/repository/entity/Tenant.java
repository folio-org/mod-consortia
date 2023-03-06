package org.folio.consortia.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Tenant {
  @Id
  @Column(name = "tenant_id", updatable = false, nullable = false)
  private String tenantId;
  private String tenantName;
}
