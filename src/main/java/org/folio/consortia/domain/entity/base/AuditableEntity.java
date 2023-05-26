package org.folio.consortia.domain.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@ToString
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

  @CreatedDate
  @Column(name = "created_date", nullable = false, updatable = false)
  private Timestamp createdDate;

  @CreatedBy
  @Column(name = "created_by", updatable = false)
  private UUID createdBy;

  @LastModifiedDate
  @Column(name = "updated_date")
  private Timestamp updatedDate;

  @LastModifiedBy
  @Column(name = "updated_by")
  private UUID updatedBy;
}
