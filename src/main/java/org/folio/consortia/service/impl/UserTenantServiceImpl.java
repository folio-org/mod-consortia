package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.domain.repository.UserTenantRepository;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.service.UserTenantService;
import org.folio.spring.data.OffsetRequest;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
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
    Page<UserTenantEntity> userTenantPage = userTenantRepository.findAll(OffsetRequest.of(offset, limit));
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
  public UserTenantCollection getByUserId(UUID userId, Integer offset, Integer limit) {
    var result = new UserTenantCollection();
    Page<UserTenantEntity> userTenantPage = userTenantRepository.findByUserId(userId, OffsetRequest.of(offset, limit));
    result.setUserTenants(userTenantPage.stream().map(o -> converter.convert(o, UserTenant.class)).toList());
    result.setTotalRecords((int) userTenantPage.getTotalElements());

    if (userTenantPage.getContent().isEmpty()) {
      throw new ResourceNotFoundException("userId", String.valueOf(userId));
    }

    return result;
  }

  @Override
  public UserTenantCollection getByUsernameAndTenantId(String username, String tenantId) {
    if (tenantId == null) {
      throw new IllegalArgumentException("tenantId must be provided");
    }

    var result = new UserTenantCollection();

    Optional<UserTenantEntity> userTenantEntity = userTenantRepository.findByUsernameAndTenantId(username, tenantId);

    if (userTenantEntity.isEmpty()) {
      throw new ResourceNotFoundException("username", username);
    }

    UserTenant userTenant = converter.convert(userTenantEntity.get(), UserTenant.class);
    result.setUserTenants(List.of(userTenant));
    result.setTotalRecords(1);
    return result;
  }

}
