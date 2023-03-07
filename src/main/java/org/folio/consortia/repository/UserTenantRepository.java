package org.folio.consortia.repository;

import org.folio.consortia.entity.UserTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTenantRepository extends JpaRepository<UserTenant, String> {
}
