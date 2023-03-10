package org.folio.consortia.domain.repository;

import org.folio.consortia.domain.entity.UserTenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserTenantRepository extends JpaRepository<UserTenantEntity, UUID> {
  UserTenantEntity findByUserId(UUID id);
  UserTenantEntity findByUsername(String username);
}
