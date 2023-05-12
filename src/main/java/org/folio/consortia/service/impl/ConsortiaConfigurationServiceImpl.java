package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.entity.ConsortiaConfigurationEntity;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.repository.ConsortiaConfigurationRepository;
import org.folio.consortia.service.ConsortiaConfigurationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ConsortiaConfigurationServiceImpl implements ConsortiaConfigurationService {
  private static final String CONSORTIA_CONFIGURATION_EXIST_MSG_TEMPLATE = "System can not have more than one configuration record";
  private final ConsortiaConfigurationRepository configurationRepository;

  @Override
  public String getCentralTenant() {
    List<ConsortiaConfigurationEntity> configList = configurationRepository.findAll();
    if (configList.isEmpty()) {
      throw new ResourceNotFoundException("A central tenant not found in this tenant '{}' configuration ");
    }
    return configList.get(0).getCentralTenantId();
  }

  @Override
  public void createConfiguration(String centralTenantId) {
    checkAnyConsortiaConfigurationNotExistsOrThrow();
    ConsortiaConfigurationEntity configuration = new ConsortiaConfigurationEntity();
    configuration.setCentralTenantId(centralTenantId);
    configurationRepository.save(configuration);
  }

  private void checkAnyConsortiaConfigurationNotExistsOrThrow() {
    if (configurationRepository.count() > 0) {
      throw new ResourceAlreadyExistException(CONSORTIA_CONFIGURATION_EXIST_MSG_TEMPLATE);
    }
  }

}
