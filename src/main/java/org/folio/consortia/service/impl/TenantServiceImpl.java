package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.service.TenantService;
import org.folio.pv.domain.dto.TenantCollection;
import org.folio.consortia.repository.TenantRepository;
import org.folio.consortia.repository.entity.Tenant;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@EnableScheduling
@Log4j2
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
  private final TenantRepository repository;

  @Transactional(readOnly = true)
  @Override
  public TenantCollection get() {
    var result = new TenantCollection();
    List<Tenant> tenantList = repository.findAll();
    result.setTenants(tenantList.stream().map(TenantServiceImpl::entityToDto).toList());
    result.setTotalRecords(tenantList.size());

    return result;
  }

  public static org.folio.pv.domain.dto.Tenant entityToDto(Tenant entity) {
    var result = new org.folio.pv.domain.dto.Tenant();

    result.setTenantId(entity.getTenantId());
    result.setTenantName(entity.getTenantName());

    return result;
  }

}
