package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.entity.UserTenant;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.UserTenantService;
import org.folio.pv.domain.dto.UserTenantCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableScheduling
@Log4j2
@RequiredArgsConstructor
public class UserTenantServiceImpl implements UserTenantService {

  private final UserTenantRepository userTenantRepository;

  @Transactional(readOnly = true)
  @Override
  public UserTenantCollection get(int offset, int limit) {
    var result = new UserTenantCollection();
    Page<UserTenant> userTenantPage = userTenantRepository.findAll(PageRequest.of(offset, limit));
    result.setUserTenants(userTenantPage.stream().map(this::entityToDto).toList());
    result.setTotalRecords(userTenantPage.getSize());
    return result;
  }

  public org.folio.pv.domain.dto.UserTenant entityToDto(UserTenant userTenant) {
    var dto = new org.folio.pv.domain.dto.UserTenant();

    dto.setUserId(userTenant.getUserId());
    dto.setTenantId(userTenant.getTenantId());
    dto.setUsername(userTenant.getUsername());
    dto.setIsPrimary(userTenant.getIsPrimary());

    return dto;
  }
}
