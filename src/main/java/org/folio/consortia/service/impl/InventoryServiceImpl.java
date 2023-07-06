package org.folio.consortia.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.client.InventoryClient;
import org.folio.consortia.service.InventoryService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
  private final InventoryClient inventoryClient;
  @Override
  public String getById(UUID instanceId) {
    log.debug("getById:: parameters instanceId: {}", instanceId);
    return inventoryClient.getInstanceById(String.valueOf(instanceId));
  }

  @Override
  public void saveInstance(String instance) {
    log.debug("saveInstance:: Trying to save an instance");
    inventoryClient.saveInstance(instance);
  }
}
