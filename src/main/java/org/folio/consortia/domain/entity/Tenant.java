package org.folio.consortia.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Tenant {
  @Id
  private String id;
  private String name;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    Tenant tenant = (Tenant) o;
    return id != null && Objects.equals(id, tenant.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
