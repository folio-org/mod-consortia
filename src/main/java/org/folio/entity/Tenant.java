package org.folio.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Tenant {
  @Id
  @Column(name = "tenant_id", updatable = false, nullable = false)
  private String tenantId;
  private String tenantName;
}
