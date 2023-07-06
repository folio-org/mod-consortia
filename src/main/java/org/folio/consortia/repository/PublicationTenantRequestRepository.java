package org.folio.consortia.repository;

import java.util.UUID;

import org.folio.consortia.domain.entity.PublicationTenantRequestEntity;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicationTenantRequestRepository extends JpaRepository<PublicationTenantRequestEntity, UUID> {

  Page<PublicationTenantRequestEntity> findByPcState_Id(UUID publicationId, Pageable pageable);

}
