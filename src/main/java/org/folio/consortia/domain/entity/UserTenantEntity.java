package org.folio.consortia.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "user_tenant")
public class UserTenantEntity {

  @Id
  @GeneratedValue
  @Column(name = "user_id", updatable = false, nullable = false)
  private UUID userId;

  private String username;

  @Column(name = "tenant_id")
  private String tenantId;
  @Column(name = "is_primary")
  private Boolean isPrimary;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    UserTenantEntity that = (UserTenantEntity) o;
    return getUserId() != null && Objects.equals(getUserId(), that.getUserId());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
