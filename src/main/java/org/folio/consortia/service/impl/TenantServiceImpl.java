package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.repository.TenantRepository;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.ConfigurationService;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.TenantService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.folio.consortia.utils.HelperUtils.checkIdenticalOrThrow;
import static org.folio.consortia.utils.TenantContextUtils.createFolioExecutionContextForTenant;
import static org.folio.consortia.utils.TenantContextUtils.runInFolioContext;

@Service
@Log4j2
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

  private static final String TENANTS_IDS_NOT_MATCHED_ERROR_MSG = "Request body tenantId and path param tenantId should be identical";
  private static final String TENANT_HAS_ACTIVE_USER_ASSOCIATIONS_ERROR_MSG = "Cannot delete tenant with ID {tenantId} because it has an association with a user. " +
    "Please remove the user association before attempting to delete the tenant.";
  private final TenantRepository tenantRepository;
  private final UserTenantRepository userTenantRepository;
  private final ConversionService converter;
  private final ConsortiumService consortiumService;
  private final FolioExecutionContext folioExecutionContext;
  private final FolioModuleMetadata folioMetadata;
  private final ConfigurationService configurationService;

  @Override
  public TenantCollection get(UUID consortiumId, Integer offset, Integer limit) {
    TenantCollection result = new TenantCollection();
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    Page<TenantEntity> page = tenantRepository.findByConsortiumId(consortiumId, PageRequest.of(offset, limit));
    result.setTenants(page.map(o -> converter.convert(o, Tenant.class)).getContent());
    result.setTotalRecords((int) page.getTotalElements());
    return result;
  }

  @Override
  public String getCentralTenantId() {
    TenantEntity tenant = tenantRepository.findCentralTenant()
      .orElseThrow(() -> new ResourceNotFoundException("A central tenant is not found. The central tenant must be created"));
    return tenant.getId();
  }

  @Override
  public TenantEntity getByTenantId(String tenantId) {
    return tenantRepository.findById(tenantId)
      .orElse(null);
  }

  @Override
  public Tenant save(UUID consortiumId, Tenant tenantDto) {
    FolioExecutionContext currentTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();
    String centralTenantId;

    if (tenantDto.getIsCentral()) {
      checkCentralTenantExistsOrThrow();
      centralTenantId = tenantDto.getId();
    } else {
      centralTenantId = getCentralTenantId();
      runInFolioContext(createFolioExecutionContextForTenant(tenantDto.getId(), currentTenantContext, folioMetadata),
        () -> configurationService.saveConfiguration(centralTenantId));
    }

    try (var context = new FolioExecutionContextSetter(createFolioExecutionContextForTenant(centralTenantId,
      currentTenantContext, folioMetadata))) {
      checkTenantNotExistsAndConsortiumExistsOrThrow(consortiumId, tenantDto.getId());
      return saveTenant(consortiumId, tenantDto);
    }
  }

  @Override
  public Tenant update(UUID consortiumId, String tenantId, Tenant tenantDto) {
    checkTenantAndConsortiumExistsOrThrow(consortiumId, tenantId);
    checkIdenticalOrThrow(tenantId, tenantDto.getId(), TENANTS_IDS_NOT_MATCHED_ERROR_MSG);
    return saveTenant(consortiumId, tenantDto);
  }

  @Override
  public void delete(UUID consortiumId, String tenantId) {
    checkTenantAndConsortiumExistsOrThrow(consortiumId, tenantId);
    if (userTenantRepository.existsByTenantId(tenantId)) {
      throw new IllegalArgumentException(TENANT_HAS_ACTIVE_USER_ASSOCIATIONS_ERROR_MSG);
    }
    tenantRepository.deleteById(tenantId);
  }

  private Tenant saveTenant(UUID consortiumId, Tenant tenantDto) {
    TenantEntity entity = toEntity(consortiumId, tenantDto);
    TenantEntity savedTenant = tenantRepository.save(entity);
    return converter.convert(savedTenant, Tenant.class);
  }

  private void checkTenantNotExistsAndConsortiumExistsOrThrow(UUID consortiumId, String tenantId) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    if (tenantRepository.existsById(tenantId)) {
      throw new ResourceAlreadyExistException("id", tenantId);
    }
  }

  private void checkTenantAndConsortiumExistsOrThrow(UUID consortiumId, String tenantId) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    if (!tenantRepository.existsById(tenantId)) {
      throw new ResourceNotFoundException("id", tenantId);
    }
  }

  private void checkCentralTenantExistsOrThrow() {
    if (tenantRepository.existsByIsCentralTrue()) {
      throw new ResourceAlreadyExistException("isCentral", "true");
    }
  }

  private TenantEntity toEntity(UUID consortiumId, Tenant tenantDto) {
    TenantEntity entity = new TenantEntity();
    entity.setId(tenantDto.getId());
    entity.setName(tenantDto.getName());
    entity.setCode(tenantDto.getCode());
    entity.setIsCentral(tenantDto.getIsCentral());
    entity.setConsortiumId(consortiumId);
    return entity;
  }
}
