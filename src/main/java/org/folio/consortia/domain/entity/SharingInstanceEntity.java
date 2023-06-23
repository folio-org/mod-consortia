package org.folio.consortia.domain.entity;

import java.util.Objects;
import java.util.UUID;

import org.folio.consortia.domain.entity.base.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "sharing_instance")
public class SharingInstanceEntity extends AuditableEntity {
  @Id
  @GeneratedValue
  private UUID id;
  private UUID instanceId;
  private String sourceTenantId;
  private String targetTenantId;
  @Enumerated(EnumType.STRING)
  private StatusType status;
  private String error;

  public enum StatusType{
    IN_PROGRESS,
    COMPLETE,
    ERROR
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SharingInstanceEntity that)) return false;
    return Objects.equals(id, that.id)
      && Objects.equals(instanceId, that.instanceId)
      && Objects.equals(sourceTenantId, that.sourceTenantId)
      && Objects.equals(targetTenantId, that.targetTenantId)
      && status == that.status
      && Objects.equals(error, that.error);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, instanceId, sourceTenantId, targetTenantId, status, error);
  }
}