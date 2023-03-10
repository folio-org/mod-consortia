package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.consortia.domain.converter.UserTenantConverter;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.domain.repository.UserTenantRepository;
import org.folio.consortia.exception.UserTenantNotFoundException;
import org.folio.consortia.service.UserTenantService;
import org.folio.pv.domain.dto.UserTenant;
import org.folio.pv.domain.dto.UserTenantCollection;
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

  @Transactional(readOnly = true)
  @Override
  public UserTenantCollection get(UUID userId, String username, Integer offset, Integer limit) {

    if (userId != null) {
      return getByUserId(userId);
    } else if (username != null) {
      return getByUsername(username);
    }
    return getAll(offset, limit);

  }

  @Override
  public UserTenant getById(UUID id) {
    Optional<UserTenantEntity> userTenantEntity = userTenantRepository.findById(id);
    if (userTenantEntity.isEmpty()) {
      throw new UserTenantNotFoundException("associationId", id);
    }
    return UserTenantConverter.toDto(userTenantEntity.get());
  }

  public UserTenantCollection getByUserId(UUID userId) {
    var result = new UserTenantCollection();
    UserTenantEntity userTenantEntity = userTenantRepository.findByUserId(userId)
      .orElseThrow(() -> new UserTenantNotFoundException("userId", userId));
    result.setUserTenants(List.of(UserTenantConverter.toDto(userTenantEntity)));
    result.setTotalRecords(1);
    return result;
  }

  public UserTenantCollection getByUsername(String username) {
    var result = new UserTenantCollection();
    List<UserTenant> userTenants = userTenantRepository.findByUsername(username)
      .stream().map(UserTenantConverter::toDto).toList();
    result.setUserTenants(userTenants);
    result.setTotalRecords(userTenants.size());
    return result;
  }

  private UserTenantCollection getAll(Integer offset, Integer limit) {
    var result = new UserTenantCollection();
    Page<UserTenantEntity> userTenantPage = userTenantRepository.findAll(PageRequest.of(offset, limit));
    result.setUserTenants(userTenantPage.stream().map(UserTenantConverter::toDto).toList());
    result.setTotalRecords((int) userTenantPage.getTotalElements());
    return result;
  }

}
