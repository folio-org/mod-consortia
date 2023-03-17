package org.folio.consortia.domain.repository;

import org.folio.consortia.domain.entity.UserTenantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTenantRepository extends JpaRepository<UserTenantEntity, UUID> {
  @Query("SELECT ut FROM UserTenantEntity ut WHERE ut.tenant.consortium.id = :consortiumId AND ut.userId = :userId")
  Page<UserTenantEntity> findByUserIdAndTenantConsortiumId(UUID consortiumId, UUID userId, Pageable pageable);

  @Query("SELECT ut FROM UserTenantEntity ut WHERE ut.tenant.consortium.id = :consortiumId AND ut.username = :username AND ut.tenant.id = :tenantId")
  Optional<UserTenantEntity> findByUsernameAndTenantIdAndTenantConsortiumId(UUID consortiumId, String username, String tenantId);

  @Query("SELECT ut FROM UserTenantEntity ut WHERE ut.tenant.consortium.id = :consortiumId AND ut.id = :associationId")
  Optional<UserTenantEntity> findByIdAndTenantConsortiumId(UUID consortiumId, UUID associationId);
}
