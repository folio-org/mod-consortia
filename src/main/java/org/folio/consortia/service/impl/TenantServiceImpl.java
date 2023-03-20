package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.dto.Tenant;
import org.folio.consortia.domain.dto.TenantCollection;
import org.folio.consortia.domain.entity.ConsortiumEntity;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.domain.repository.ConsortiumRepository;
import org.folio.consortia.domain.repository.TenantRepository;
import org.folio.consortia.exception.ResourceAlreadyExistException;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.service.TenantService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

  private final TenantRepository repository;
  private final ConsortiumRepository consortiumRepository;
  private final ConversionService converter;

  @Override
  public TenantCollection get(UUID consortiumId, Integer offset, Integer limit) {
    var result = new TenantCollection();
    validateConsortiumId(consortiumId);
    Page<TenantEntity> page = repository.findByConsortiumId(consortiumId, PageRequest.of(offset, limit));
    result.setTenants(page.map(o -> converter.convert(o, Tenant.class)).getContent());
    result.setTotalRecords((int) page.getTotalElements());
    return result;
  }

  @Override
  public Tenant save(UUID consortiumId, Tenant tenantDto) {
    validateConsortiumId(consortiumId);
    validateTenant(tenantDto.getId());
    TenantEntity entity = Objects.requireNonNull(converter.convert(tenantDto, TenantEntity.class));
    entity.setConsortiumId(consortiumId);
    TenantEntity tenantEntity = repository.save(entity);
    return converter.convert(tenantEntity, Tenant.class);
  }

  private ConsortiumEntity validateConsortiumId(UUID consortiumId) {
    return consortiumRepository.findById(consortiumId).orElseThrow(() -> new ResourceNotFoundException("id", String.valueOf(consortiumId)));
  }

  private void validateTenant(String tenantId) {
    repository.findById(tenantId).ifPresent(s -> { throw new ResourceAlreadyExistException("id", tenantId); });
  }
}
