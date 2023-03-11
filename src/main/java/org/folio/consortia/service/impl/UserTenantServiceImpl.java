package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.converter.UserTenantConverter;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.domain.repository.UserTenantRepository;
import org.folio.consortia.exception.UserTenantNotFoundException;
import org.folio.consortia.service.UserTenantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserTenantServiceImpl implements UserTenantService {

  private final UserTenantRepository userTenantRepository;
  private final UserTenantConverter converter;

  @Transactional(readOnly = true)
  @Override
  public UserTenantCollection get(Integer offset, Integer limit) {
    var result = new UserTenantCollection();
    Page<UserTenantEntity> userTenantPage = userTenantRepository.findAll(PageRequest.of(offset, limit));
    result.setUserTenants(userTenantPage.stream().map(converter::toDto).toList());
    result.setTotalRecords((int) userTenantPage.getTotalElements());
    return result;
  }

  @Override
  public UserTenant getById(UUID id) {
    Optional<UserTenantEntity> userTenantEntity = userTenantRepository.findById(id);
    if (userTenantEntity.isEmpty()) {
      throw new UserTenantNotFoundException("associationId", String.valueOf(id));
    }
    return converter.toDto(userTenantEntity.get());
  }

  @Transactional(readOnly = true)
  @Override
  public UserTenantCollection getByUserId(UUID userId) {
    var result = new UserTenantCollection();
    UserTenantEntity userTenantEntity = userTenantRepository.findByUserId(userId).orElseThrow(() -> new UserTenantNotFoundException("userId", String.valueOf(userId)));
    result.setUserTenants(List.of(converter.toDto(userTenantEntity)));
    result.setTotalRecords(1);
    return result;
  }

  @Transactional(readOnly = true)
  @Override
  public UserTenantCollection getByUsername(String username, String tenantId) {
    var result = new UserTenantCollection();

    List<UserTenantEntity> userTenants =
      tenantId != null
        ? userTenantRepository.findByUsernameAndTenantId(username, tenantId)
        : userTenantRepository.findByUsername(username);

    if (userTenants.isEmpty()) {
      throw new UserTenantNotFoundException("username", username);
    }

    result.setUserTenants(userTenants.stream().map(converter::toDto).toList());
    result.setTotalRecords(userTenants.size());
    return result;
  }

}
