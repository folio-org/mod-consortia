package org.folio.consortia.service;

import jakarta.persistence.EntityManager;

public interface LockService {

  /**
   * Lock tenant setup inside transaction. Unlock is done automatically on the transaction end.
   */
  void lockTenantSetupWithinTransaction();

  /**
   * Lock tenant setup
   */
  void lockTenantSetup(EntityManager entityManager);

  /**
   * Unlock tenant setup
   */
  void unlockTenantSetup(EntityManager entityManager);
}
