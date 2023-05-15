package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.dto.ConsortiaConfiguration;
import org.folio.consortia.domain.entity.ConsortiaConfigurationEntity;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.repository.ConsortiaConfigurationRepository;
import org.folio.consortia.service.ConsortiaConfigurationService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.folio.consortia.utils.TenantContextUtils.getTenantIdFromHeader;

@Log4j2
@Service
@RequiredArgsConstructor
public class ConsortiaConfigurationServiceImpl implements ConsortiaConfigurationService {
  private static final String CONSORTIA_CONFIGURATION_EXIST_MSG_TEMPLATE =
    "System can not have more than one configuration record";
  private final ConsortiaConfigurationRepository configurationRepository;
  private final ConversionService converter;
  private final FolioExecutionContext folioExecutionContext;
  private final FolioModuleMetadata folioMetadata;

  @Override
  public ConsortiaConfiguration getConsortiaConfiguration() {
    FolioExecutionContext currentContext = (FolioExecutionContext) folioExecutionContext.getInstance();
    String requestedTenantId = getTenantIdFromHeader(currentContext);
    ConsortiaConfigurationEntity configuration;

    // getting central tenant for this requested tenant from saved configuration in its own schema
//    try (var context = new FolioExecutionContextSetter(createFolioExecutionContextForTenant(
//      requestedTenantId, folioExecutionContext, folioMetadata))) {
    configuration = getConfiguration(requestedTenantId);
//    }

    return converter.convert(configuration, ConsortiaConfiguration.class);
  }

  @Override
  public String getCentralTenantId(String requestTenantId) {
    return getConfiguration(requestTenantId).getCentralTenantId();
  }

  @Override
  public ConsortiaConfiguration createConfiguration(String centralTenantId) {
    checkAnyConsortiaConfigurationNotExistsOrThrow();
    ConsortiaConfigurationEntity configuration = new ConsortiaConfigurationEntity();
    configuration.setCentralTenantId(centralTenantId);
    return converter.convert(configurationRepository.save(configuration), ConsortiaConfiguration.class);
  }

//  @Override
//  public ConsortiaConfiguration createConfigurationByFolioExecutionContext() {
//    FolioExecutionContext currentContext = (FolioExecutionContext) folioExecutionContext.getInstance();
//    String requestedTenantId = getTenantIdFromHeader(currentContext);
//    ConsortiaConfigurationEntity configuration;
//
//    String centralTenantId = getCentralTenantId(requestedTenantId);
//
//    // getting central tenant for this requested tenant from saved configuration in its own schema
////    try (var context = new FolioExecutionContextSetter(createFolioExecutionContextForTenant(
////      requestedTenantId, folioExecutionContext, folioMetadata))) {
//    configuration = createConfiguration(centralTenantId);
////    }
//
//    return converter.convert(configuration, ConsortiaConfiguration.class);
//  }

  private ConsortiaConfigurationEntity getConfiguration(String requestTenantId) {
    List<ConsortiaConfigurationEntity> configList = configurationRepository.findAll();
    if (configList.isEmpty()) {
      throw new ResourceNotFoundException("A central tenant not found in this tenant '{}' configuration", requestTenantId);
    }
    return configList.get(0);
  }

  private void checkAnyConsortiaConfigurationNotExistsOrThrow() {
    if (configurationRepository.count() > 0) {
      throw new ResourceAlreadyExistException(CONSORTIA_CONFIGURATION_EXIST_MSG_TEMPLATE);
    }
  }

}
