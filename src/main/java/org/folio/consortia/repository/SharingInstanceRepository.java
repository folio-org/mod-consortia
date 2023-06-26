package org.folio.consortia.repository;

import java.util.UUID;

import org.folio.consortia.domain.entity.SharingInstanceEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SharingInstanceRepository
  extends JpaRepository<SharingInstanceEntity, UUID>,
  JpaSpecificationExecutor<SharingInstanceEntity> {

  interface Specifications {
    static Specification<SharingInstanceEntity> byInstanceIdentifier(UUID instanceId) {
      if(instanceId == null) return null;
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("instanceId"), instanceId);
    }

    static Specification<SharingInstanceEntity> bySourceTenantId(String sourceTenantId) {
      if(sourceTenantId == null) return null;
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("sourceTenantId"), sourceTenantId);
    }

    static Specification<SharingInstanceEntity> byTargetTenantId(String targetTenantId) {
      if(targetTenantId == null) return null;
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("targetTenantId"), targetTenantId);
    }

    static Specification<SharingInstanceEntity> byStatusType(String status) {
      if(status == null) return null;
      var statusType = SharingInstanceEntity.StatusType.valueOf(status);
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), statusType);
    }
  }
}
