package org.folio.consortia.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

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
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "pc_state")
public class PublicationTenantRequestEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  private UUID pcId;
  private String tenantId;
  private String status;
  private String requestUrl;
  private String requestPayload;
  private String response;
  private Integer responseStatusCode;

  @CreatedDate
  @Column(name = "started_date", nullable = false, updatable = false)
  private LocalDateTime startedDate;

  @LastModifiedDate
  @Column(name = "completed_date", nullable = false, updatable = false)
  private LocalDateTime completedDate;

}
