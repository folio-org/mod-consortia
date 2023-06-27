package org.folio.consortia.repository;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.UUID;

import org.folio.consortia.domain.entity.SharingInstanceEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SharingInstanceRepository
  extends JpaRepository<SharingInstanceEntity, UUID>, JpaSpecificationExecutor<SharingInstanceEntity> {

  interface Specifications {
    static Specification<SharingInstanceEntity> constructSpecification(UUID instanceIdentifier, String sourceTenantId,
        String targetTenantId, String status) {
      var list = new ArrayList<Specification<SharingInstanceEntity>>();
      if(nonNull(instanceIdentifier)) list.add(byInstanceIdentifier(instanceIdentifier));
      if(nonNull(sourceTenantId)) list.add(bySourceTenantId(sourceTenantId));
      if(nonNull(targetTenantId)) list.add(byTargetTenantId(targetTenantId));
      if(nonNull(status)) list.add(byStatusType(status));

      return list.stream().reduce(Specification::and).orElse(null);
    }

    static Specification<SharingInstanceEntity> byInstanceIdentifier(UUID instanceId) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("instanceId"), instanceId);
    }

    static Specification<SharingInstanceEntity> bySourceTenantId(String sourceTenantId) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("sourceTenantId"), sourceTenantId);
    }

    static Specification<SharingInstanceEntity> byTargetTenantId(String targetTenantId) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("targetTenantId"), targetTenantId);
    }

    static Specification<SharingInstanceEntity> byStatusType(String status) {
      var statusType = SharingInstanceEntity.StatusType.valueOf(status);
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), statusType);
    }
  }
}
