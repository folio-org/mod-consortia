package org.folio.consortia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.folio.consortia.client.UsersClient;
import org.folio.consortia.domain.dto.Personal;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.domain.repository.UserTenantRepository;
import org.folio.consortia.exception.PrimaryAffiliationException;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.UserTenantService;
import org.folio.consortia.utils.HelperUtils;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.scope.FolioExecutionScopeExecutionContextManager;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link UserTenantService}.
 *
 * Consortium table will contain only a single record and it will be prohibited to add another record in this table.
 * If another consortium will be created - a new separate DB schema for it will be created that also stores only a
 * single record in the consortium table. So to simplify logic and source code it was decided that we will not store
 * consortiumId in user_tenant table.
 *
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class UserTenantServiceImpl implements UserTenantService {

  private static final String USER_ID = "userId";
  private static final Boolean IS_PRIMARY_TRUE = true;
  private static final Boolean IS_PRIMARY_FALSE = false;
  private static final Integer RANDOM_STRING_COUNT = 5;
  private static final String PATRON_GROUP = null;
  private final UserTenantRepository userTenantRepository;
  private final FolioExecutionContext folioExecutionContext;
  private final ConversionService converter;
  private final ConsortiumService consortiumService;
  private final UsersClient usersClient;
  private final FolioModuleMetadata folioModuleMetadata;

  @Override
  public UserTenantCollection get(UUID consortiumId, Integer offset, Integer limit) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    var result = new UserTenantCollection();
    Page<UserTenantEntity> userTenantPage = userTenantRepository.findAll(PageRequest.of(offset, limit));
    result.setUserTenants(userTenantPage.stream().map(o -> converter.convert(o, UserTenant.class)).toList());
    result.setTotalRecords((int) userTenantPage.getTotalElements());
    return result;
  }

  @Override
  public UserTenant getById(UUID consortiumId, UUID id) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    UserTenantEntity userTenantEntity = userTenantRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("id", String.valueOf(id)));
    return converter.convert(userTenantEntity, UserTenant.class);
  }

  @Override
  public UserTenant save(UUID consortiumId, UserTenant userTenantDto) {
    String currentTenantId = HelperUtils.getTenantId(folioExecutionContext);
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);

    Optional<UserTenantEntity> userTenant = userTenantRepository.findByUserIdAndTenantId(userTenantDto.getUserId(), IS_PRIMARY_TRUE);
      if (userTenant.isEmpty()) {
        throw new ResourceNotFoundException(USER_ID, String.valueOf(userTenantDto.getUserId()));
      } else {
      UserTenantEntity userTenantEntity = userTenant.get();
       if (Boolean.FALSE.equals(userTenantEntity.getIsPrimary())) {
         throw new PrimaryAffiliationException(USER_ID, String.valueOf(userTenantDto.getUserId()));
       }
     }

    prepareContextForTenant(userTenant.get().getTenant().getId());
    User shadowUser = prepareShadowUser(userTenantDto.getUserId(), userTenantDto);
    validateAndUpdateShadowUser(userTenantDto.getUserId());

    prepareContextForTenant(currentTenantId);
    UserTenantEntity userTenantEntity = toEntity(userTenantDto, consortiumId, shadowUser);
    userTenantRepository.save(userTenantEntity);

    return converter.convert(userTenantEntity, UserTenant.class);
  }

  @Override
  public UserTenantCollection getByUserId(UUID consortiumId, UUID userId, Integer offset, Integer limit) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    var result = new UserTenantCollection();
    Page<UserTenantEntity> userTenantPage = userTenantRepository.findByUserId(userId, PageRequest.of(offset, limit));

    if (userTenantPage.getContent().isEmpty()) {
      throw new ResourceNotFoundException(USER_ID, String.valueOf(userId));
    }

    result.setUserTenants(userTenantPage.stream().map(o -> converter.convert(o, UserTenant.class)).toList());
    result.setTotalRecords((int) userTenantPage.getTotalElements());
    return result;
  }

  @Override
  public UserTenantCollection getByUsernameAndTenantId(UUID consortiumId, String username, String tenantId) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    var result = new UserTenantCollection();
    UserTenantEntity userTenantEntity = userTenantRepository.findByUsernameAndTenantId(username, tenantId)
      .orElseThrow(() -> new ResourceNotFoundException("username", username));
    UserTenant userTenant = converter.convert(userTenantEntity, UserTenant.class);

    result.setUserTenants(List.of(userTenant));
    result.setTotalRecords(1);
    return result;
  }

  private User prepareShadowUser(UUID userId, UserTenant userTenantDto) {
    User userOptional = getUser(userId);
    User user = new User();
    if (Objects.nonNull(userOptional.getId())) {
      user.setId(UUID.randomUUID().toString());
      user.setPatronGroup(PATRON_GROUP);
      user.setUsername(userOptional.getUsername() + RandomStringUtils.randomAlphabetic(RANDOM_STRING_COUNT));
      if(Objects.nonNull(userOptional.getPersonal())) {
        Personal personal = new Personal();
        personal.setLastName(userOptional.getPersonal().getLastName());
        personal.setFirstName(userOptional.getPersonal().getFirstName());
        personal.setEmail(userOptional.getPersonal().getEmail());
        personal.setPreferredContactTypeId(userOptional.getPersonal().getPreferredContactTypeId());
        user.setPersonal(personal);
      }
      user.setPatronGroup(userOptional.getPatronGroup());
      user.setActive(IS_PRIMARY_TRUE);
    } else {
      throw new ResourceNotFoundException(USER_ID, userId.toString());
    }
    prepareContextForTenant(userTenantDto.getTenantId());
    return user;
  }

  private void validateAndUpdateShadowUser(UUID userId) {
    updateUser(getUser(userId));
  }

  private User getUser(UUID userId) {
    log.info("Getting user by userId {}.", userId);
    return usersClient.getUsersByUserId(String.valueOf(userId));
  }

  private void updateUser(User user) {
    if (HelperUtils.existingUserUpToDate(user)) {
      log.info("{} is up to date.", user);
    } else {
      user.setActive(IS_PRIMARY_TRUE);
      log.info("Updating {}.", user);
      usersClient.updateUser(user.getId(), user);
    }
  }

  private void prepareContextForTenant(String tenantId) {
    if (MapUtils.isNotEmpty(folioExecutionContext.getOkapiHeaders())) {
      folioExecutionContext.getOkapiHeaders().put("x-okapi-tenant", List.of(tenantId));
      FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext(
        new DefaultFolioExecutionContext(folioModuleMetadata, folioExecutionContext.getOkapiHeaders()));

      log.info("FOLIO context initialized with tenant {}", folioExecutionContext.getTenantId());
    }
  }

  private UserTenantEntity toEntity(UserTenant userTenantDto, UUID consortiumId, User user) {
    UserTenantEntity entity = new UserTenantEntity();
    TenantEntity tenant = new TenantEntity();
    tenant.setId(userTenantDto.getTenantId());
    tenant.setName(userTenantDto.getTenantName());
    tenant.setConsortiumId(consortiumId);
    if(Objects.nonNull(userTenantDto.getId())) {
      entity.setId(userTenantDto.getId());
    } else {
      entity.setId(UUID.randomUUID());
    }
    entity.setUserId(userTenantDto.getUserId());
    entity.setUsername(user.getUsername());
    entity.setTenant(tenant);
    entity.setIsPrimary(IS_PRIMARY_FALSE);
    return entity;
  }
}
