package org.folio.consortia.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "tenant")
public class TenantEntity implements Persistable<String> {
  @Id
  private String id;
  private String name;
  private UUID consortiumId;
  @Transient
  private boolean isNew = true;

  @Override
  public boolean isNew() {
    return isNew;
  }

  @PrePersist
  @PostLoad
  void markNotNew() {
    this.isNew = false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TenantEntity that = (TenantEntity) o;
    return isNew == that.isNew && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(consortiumId, that.consortiumId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, consortiumId, isNew);
  }
}
