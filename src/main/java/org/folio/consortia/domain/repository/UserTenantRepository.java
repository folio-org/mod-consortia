package org.folio.consortia.domain.repository;

import org.folio.consortia.domain.entity.UserTenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTenantRepository extends JpaRepository<UserTenantEntity, UUID> {
  Optional<UserTenantEntity> findByUserId(UUID id);
  List<UserTenantEntity> findByUsername(String username);
}
