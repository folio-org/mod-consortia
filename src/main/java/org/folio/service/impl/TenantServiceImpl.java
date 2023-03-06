package org.folio.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.pv.domain.dto.Tenant;
import org.folio.pv.domain.dto.TenantCollection;
import org.folio.repository.CQLService;
import org.folio.repository.TenantRepository;
import org.folio.service.TenantService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@EnableScheduling
@Log4j2
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService{

  private final TenantRepository repository;
  private final CQLService cqlService;

  @Transactional(readOnly = true)
  @Override
  public TenantCollection get(String query, Integer offset, Integer limit) {
    var result = new TenantCollection();
    if (StringUtils.isBlank(query)) {
      List<org.folio.entity.Tenant> tenantList = repository.findAll();
      result.setTenants(tenantList.stream().map(TenantServiceImpl::entityToDto).toList());
      result.setTotalRecords(tenantList.size());
    } else {
      result.setTenants(cqlService.getByCQL(org.folio.entity.Tenant.class, query, offset, limit)
        .stream()
        .map(TenantServiceImpl::entityToDto)
        .toList());
      result.setTotalRecords(cqlService.countByCQL(Tenant.class, query));
    }
    return result;
  }

  public static org.folio.pv.domain.dto.Tenant entityToDto(org.folio.entity.Tenant entity) {
    var result = new org.folio.pv.domain.dto.Tenant();

    result.setTenantId(entity.getTenantId());
    result.setTenantName(entity.getTenantName());

    return result;
  }

}
