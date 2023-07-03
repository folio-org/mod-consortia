package org.folio.consortia.repository;

import static java.util.Objects.nonNull;
import static com.github.jknack.handlebars.internal.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.UUID;

import org.folio.consortia.domain.dto.Status;
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
        String targetTenantId, Status status) {
      var list = new ArrayList<Specification<SharingInstanceEntity>>();
      if(nonNull(instanceIdentifier)) list.add(by("instanceId", instanceIdentifier, UUID.class));
      if(isNotEmpty(sourceTenantId)) list.add(by("sourceTenantId", sourceTenantId, String.class));
      if(isNotEmpty(targetTenantId)) list.add(by("targetTenantId", targetTenantId, String.class));
      if(nonNull(status)) list.add(by("status", status, Status.class));

      return list.stream().reduce(Specification::and).orElse(null);
    }

    static Specification<SharingInstanceEntity> by(String fieldName, Object fieldValue, Class<?> clazz) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(fieldName), clazz.cast(fieldValue));
    }
  }
}
