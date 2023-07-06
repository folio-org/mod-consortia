package org.folio.consortia.service;

import java.util.UUID;

public interface InventoryService {

  /**
   * Get an instance by id
   * @param instanceId UUID of the instance
   * @return Instance as String
   */
  String getById(UUID instanceId);

  /**
   * Create instance.
   * @param instance instance.
   */
  void saveInstance(String instance);
}
