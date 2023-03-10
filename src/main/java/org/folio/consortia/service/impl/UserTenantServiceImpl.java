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
    var result = new UserTenantCollection();
    Page<UserTenantEntity> userTenantPage;
    if (userId != null) {
      UserTenant userTenant = UserTenantConverter.toDto(userTenantRepository.findByUserId(userId));
      result.setUserTenants(List.of(userTenant));
      result.setTotalRecords(result.getUserTenants().size());
      return result;
    }
    if (username != null) {
      UserTenant userTenant = UserTenantConverter.toDto(userTenantRepository.findByUsername(username));
      result.setUserTenants(List.of(userTenant));
      result.setTotalRecords(result.getUserTenants().size());
      return result;
    }
    userTenantPage = userTenantRepository.findAll(PageRequest.of(offset, limit));
    result.setUserTenants(userTenantPage.stream().map(UserTenantConverter::toDto).toList());
    result.setTotalRecords(userTenantPage.getSize());
    return result;
  }

  @Override
  public UserTenant getById(UUID id) {
    Optional<UserTenantEntity> userTenantEntity = userTenantRepository.findById(id);
    if (userTenantEntity.isEmpty()) {
      throw new UserTenantNotFoundException(id);
    }
    return UserTenantConverter.toDto(userTenantEntity.get());
  }

}
