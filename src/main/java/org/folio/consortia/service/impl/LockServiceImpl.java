package org.folio.consortia.service.impl;

import org.folio.consortia.service.LockService;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class LockServiceImpl implements LockService {
  //identifiers of advisory lock for tenant setup
  private static final String TENANT_SETUP_LOCK_ID_PARAMS = "23082023, 1008025656";
  private static final String TENANT_SETUP_TRANSACTIONAL_LOCK_SQL = String.format("SELECT pg_advisory_xact_lock(%s)",
    TENANT_SETUP_LOCK_ID_PARAMS);
  private static final String TENANT_SETUP_SESSION_LOCK_SQL = String.format("SELECT pg_advisory_lock(%s)",
    TENANT_SETUP_LOCK_ID_PARAMS);
  private static final String TENANT_SETUP_SESSION_UNLOCK_SQL = String.format("SELECT pg_advisory_unlock(%s)",
    TENANT_SETUP_LOCK_ID_PARAMS);

  private static final String SETUP_LOCAL_LOCK_TIMEOUT_SQL = "SET LOCAL lock_timeout = '300000ms'";

  @PersistenceContext
  private final EntityManager applicationEntityManager;

  @Override
  public void lockTenantSetupWithinTransaction() {
    log.info("lockTenantSetupWithinTransaction:: attempting to acquire lock");

    setLocalLockTimeout(applicationEntityManager);
    applicationEntityManager.createNativeQuery(TENANT_SETUP_TRANSACTIONAL_LOCK_SQL).getSingleResult();

    log.info("lockTenantSetupTransactional:: lock acquired");
  }

  @Override
  public void lockTenantSetup(EntityManager entityManager) {
    log.info("lockTenantSetup:: attempting to acquire lock");

    try {
      // need to handle transaction manually since it's not an application entityManager provided by spring
      entityManager.getTransaction().begin();

      setLocalLockTimeout(entityManager);
      entityManager.createNativeQuery(TENANT_SETUP_SESSION_LOCK_SQL)
        .getSingleResult();

      entityManager.getTransaction().commit();
      log.info("lockTenantSetup:: lock acquired");
    } catch (Exception e) {
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().rollback();
      }
      throw e;
    }
  }

  @Override
  public void unlockTenantSetup(EntityManager entityManager) {
    log.info("unlockTenantSetup:: attempting to release lock");
    entityManager.createNativeQuery(TENANT_SETUP_SESSION_UNLOCK_SQL).getSingleResult();
    log.info("unlockTenantSetup:: lock released");
  }

  private void setLocalLockTimeout(EntityManager entityManager) {
    entityManager.createNativeQuery(SETUP_LOCAL_LOCK_TIMEOUT_SQL).executeUpdate();
  }
}
