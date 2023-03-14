package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.domain.repository.UserTenantRepository;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.service.UserTenantService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserTenantServiceImpl implements UserTenantService {

  private final UserTenantRepository userTenantRepository;
  private final ConversionService converter;

  @Override
  public UserTenantCollection get(Integer offset, Integer limit) {
    var result = new UserTenantCollection();
    Page<UserTenantEntity> userTenantPage = userTenantRepository.findAll(PageRequest.of(offset, limit));
    result.setUserTenants(userTenantPage.stream().map(o -> converter.convert(o, UserTenant.class)).toList());
    result.setTotalRecords((int) userTenantPage.getTotalElements());
    return result;
  }

  @Override
  public UserTenant getById(UUID id) {
    Optional<UserTenantEntity> userTenantEntity = userTenantRepository.findById(id);
    if (userTenantEntity.isEmpty()) {
      throw new ResourceNotFoundException("associationId", String.valueOf(id));
    }
    return converter.convert(userTenantEntity.get(), UserTenant.class);
  }

  @Override
  public UserTenantCollection getByUserId(UUID userId) {
    var result = new UserTenantCollection();
    List<UserTenantEntity> userTenantEntityList = userTenantRepository.findByUserId(userId);

    if (userTenantEntityList.isEmpty()) {
      throw new ResourceNotFoundException("userId", String.valueOf(userId));
    }

    List<UserTenant> userTenantList = userTenantEntityList.stream().map(o -> converter.convert(o, UserTenant.class)).toList();
    result.setUserTenants(userTenantList);
    result.setTotalRecords(1);
    return result;
  }

  @Override
  public UserTenantCollection getByUsername(String username, String tenantId) {
    var result = new UserTenantCollection();

    List<UserTenantEntity> userTenants =
      tenantId != null
        ? userTenantRepository.findByUsernameAndTenantId(username, tenantId)
        : userTenantRepository.findByUsername(username);

    if (userTenants.isEmpty()) {
      throw new ResourceNotFoundException("username", username);
    }

    result.setUserTenants(userTenants.stream().map(o -> converter.convert(o, UserTenant.class)).toList());
    result.setTotalRecords(userTenants.size());
    return result;
  }

}
