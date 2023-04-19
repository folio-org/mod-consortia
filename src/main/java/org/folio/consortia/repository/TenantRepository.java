package org.folio.consortia.repository;

import java.util.UUID;

import org.folio.consortia.domain.entity.TenantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, String> {

  Page<TenantEntity> findByConsortiumId(UUID consortiumId, Pageable pageable);
  TenantEntity findFirstByName(String tenantId);
}
