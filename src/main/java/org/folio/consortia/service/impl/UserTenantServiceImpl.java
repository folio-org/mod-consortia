package org.folio.consortia.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.folio.consortia.client.PermissionsClient;
import org.folio.consortia.client.UsersClient;
import org.folio.consortia.domain.dto.Personal;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.domain.dto.UserEvent;
import org.folio.consortia.domain.dto.UserTenant;
import org.folio.consortia.domain.dto.UserTenantCollection;
import org.folio.consortia.domain.dto.PermissionUser;
import org.folio.consortia.domain.entity.TenantEntity;
import org.folio.consortia.domain.entity.UserTenantEntity;
import org.folio.consortia.exception.ConsortiumClientException;
import org.folio.consortia.exception.PrimaryAffiliationException;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.repository.UserTenantRepository;
import org.folio.consortia.service.ConsortiumService;
import org.folio.consortia.service.UserTenantService;
import org.folio.consortia.utils.HelperUtils;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Implementation of {@link UserTenantService}.
 * <p>
 * Consortium table will contain only a single record and it will be prohibited to add another record in this table.
 * If another consortium will be created - a new separate DB schema for it will be created that also stores only a
 * single record in the consortium table. So to simplify logic and source code it was decided that we will not store
 * consortiumId in user_tenant table.
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class UserTenantServiceImpl implements UserTenantService {
  private static final String NOT_FOUND_PRIMARY_AFFILIATION_MSG = "User with %s [%s] doesn't have primary affiliation";
  private static final String USER_ID = "userId";
  private static final String TENANT_ID = "tenantId";
  private static final Boolean IS_PRIMARY_TRUE = true;
  private static final Boolean IS_PRIMARY_FALSE = false;
  private static final Integer RANDOM_STRING_COUNT = 5;
  private static final String PATRON_GROUP = null;

  private final UserTenantRepository userTenantRepository;
  private final FolioExecutionContext folioExecutionContext;
  private final ConversionService converter;
  private final ConsortiumService consortiumService;
  private final UsersClient usersClient;
  private final PermissionsClient permissionsClient;
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

  @Override
  public UserTenant getByUsernameAndTenantIdOrNull(UUID consortiumId, String username, String tenantId) {
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    return userTenantRepository.findByUsernameAndTenantId(username, tenantId)
      .map(ute -> converter.convert(ute, UserTenant.class))
      .orElse(null);
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
  @Transactional
  public UserTenant save(UUID consortiumId, UserTenant userTenantDto) {
    log.debug("Going to save user with id: {} into tenant: {}", userTenantDto.getUserId(), userTenantDto.getTenantId());
    FolioExecutionContext currentTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();
    String currentTenantId = folioExecutionContext.getTenantId();
    consortiumService.checkConsortiumExistsOrThrow(consortiumId);

    Optional<UserTenantEntity> userTenant = userTenantRepository.findByUserIdAndIsPrimary(userTenantDto.getUserId(), IS_PRIMARY_TRUE);
    if (userTenant.isEmpty()) {
      log.warn("Could not find user '{}' with primary affiliation in user_tenant table", userTenantDto.getUserId());
      throw new ResourceNotFoundException(String.format(NOT_FOUND_PRIMARY_AFFILIATION_MSG, USER_ID, userTenantDto.getUserId()));
    }

    User shadowUser = prepareShadowUser(userTenantDto.getUserId(), userTenant.get(), currentTenantContext);
    createOrUpdateShadowUser(userTenantDto.getUserId(), shadowUser, userTenantDto, currentTenantContext);

    try (var context = new FolioExecutionContextSetter(prepareContextForTenant(currentTenantId, currentTenantContext))) {
      UserTenantEntity userTenantEntity = toEntity(userTenantDto, consortiumId, shadowUser);
      userTenantRepository.save(userTenantEntity);
      log.info("User affiliation added and user created or activated for user id: {} in the tenant: {}",
        userTenantDto.getUserId(), userTenantDto.getTenantId());

      return converter.convert(userTenantEntity, UserTenant.class);
    }
  }

  @Override
  @Transactional
  public UserTenant createPrimaryUserTenantAffiliation(UUID consortiumId, TenantEntity consortiaTenant, UserEvent userEvent) {
    UserTenantEntity userTenantEntity = new UserTenantEntity();

    userTenantEntity.setId(UUID.randomUUID());
    userTenantEntity.setUserId(UUID.fromString(userEvent.getUserDto().getId()));
    userTenantEntity.setUsername(userEvent.getUserDto().getUsername());
    userTenantEntity.setTenant(consortiaTenant);
    userTenantEntity.setIsPrimary(IS_PRIMARY_TRUE);

    var createdRecord = userTenantRepository.save(userTenantEntity);
    return converter.convert(createdRecord, UserTenant.class);
  }

  @Override
  @Transactional
  public void deleteByUserIdAndTenantId(UUID consortiumId, String tenantId, UUID userId) {
    log.debug("Going to delete user affiliation for user id: {} in the tenant: {}", userId.toString(), tenantId);
    FolioExecutionContext currentTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();

    consortiumService.checkConsortiumExistsOrThrow(consortiumId);
    UserTenantEntity userTenantEntity = userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
      .orElseThrow(() -> new ResourceNotFoundException(USER_ID + ", " + TENANT_ID, userId + ", " + tenantId));

    if (Boolean.TRUE.equals(userTenantEntity.getIsPrimary())) {
      log.warn("Primary affiliation could not be deleted from API for user id: {} in the tenant: {}",
        userId.toString(), userTenantEntity.getTenant().getId());
      throw new PrimaryAffiliationException(String.valueOf(userId), tenantId);
    }

    userTenantRepository.deleteByUserIdAndTenantId(userId, tenantId);

    try (var context = new FolioExecutionContextSetter(prepareContextForTenant(tenantId, currentTenantContext))) {
      User user = getUser(userId);
      deactivateUser(user);
      log.info("User affiliation deleted and user deactivated for user id: {} in the tenant: {}", userId.toString(), tenantId);
    }
  }

  @Override
  @Transactional
  public void deletePrimaryUserTenantAffiliation(UUID userId) {
    userTenantRepository.deleteByUserIdAndIsPrimaryTrue(userId);
  }

  @Override
  public UserTenant update(UUID consortiumId, UserTenant primary) {
    return new UserTenant();
  }

  private User prepareShadowUser(UUID userId, UserTenantEntity userTenantEntity, FolioExecutionContext folioExecutionContext) {
    try (var context = new FolioExecutionContextSetter(prepareContextForTenant(userTenantEntity.getTenant().getId(), folioExecutionContext))) {
      User user = new User();
      User userOptional = getUser(userId);

      if (Objects.nonNull(userOptional.getId())) {
        user.setId(userId.toString());
        user.setPatronGroup(PATRON_GROUP);
        user.setUsername(userOptional.getUsername() + HelperUtils.randomString(RANDOM_STRING_COUNT));
        var userPersonal = userOptional.getPersonal();
        if (Objects.nonNull(userPersonal)) {
          Personal personal = new Personal();
          personal.setLastName(userPersonal.getLastName());
          personal.setFirstName(userPersonal.getFirstName());
          personal.setEmail(userPersonal.getEmail());
          personal.setPreferredContactTypeId(userPersonal.getPreferredContactTypeId());
          user.setPersonal(personal);
        }
        user.setPatronGroup(userOptional.getPatronGroup());
        user.setActive(true);
      } else {
        log.warn("Could not find real user with id: {} in his home tenant: {}", userId.toString(), userTenantEntity.getTenant().getId());
        throw new ResourceNotFoundException(USER_ID, userId.toString());
      }
      return user;
    }
  }

  private void createOrUpdateShadowUser(UUID userId, User shadowUser, UserTenant userTenantDto, FolioExecutionContext folioExecutionContext) {
    log.info("Going to create or update shadow user with id: {} in the desired tenant: {}", userId.toString(), userTenantDto.getTenantId());
    try (var context = new FolioExecutionContextSetter(prepareContextForTenant(userTenantDto.getTenantId(), folioExecutionContext))) {
      User user = getUser(userId);
      if (Objects.nonNull(user.getActive())) {
        activateUser(user);
      } else {
        createActiveUserWithPermissions(shadowUser);
      }
    }
  }

  /**
   * Gets user by id.
   * <p>
   * This method will be called to get User from MOD_USERS module based on the tenant schema present in the folioContext.
   *
   * @param userId user id.
   */
  private User getUser(UUID userId) {
    try {
      log.info("Getting user by userId {}.", userId);
      return usersClient.getUsersByUserId(String.valueOf(userId));
    } catch (FeignException.NotFound e) {
      log.info("User with userId {} does not exist in schema, going to use new one", userId);
      return new User();
    } catch (FeignException.Forbidden e) {
      throw new ConsortiumClientException(e);
    } catch (FeignException e) {
      throw new IllegalStateException(e);
    }
  }

  private void createActiveUserWithPermissions(User user) {
    createPermissionUser(user.getId());
    log.info("Creating user with id {}.", user.getId());
    usersClient.saveUser(user);
  }

  private void activateUser(User user) {
    if (Boolean.TRUE.equals(user.getActive())) {
      log.info("User with id '{}' is already active.", user.getId());
    } else {
      user.setActive(true);
      log.info("Updating User with id '{}' with active 'true'. ", user.getId());
      usersClient.updateUser(user.getId(), user);
    }
  }

  private void deactivateUser(User user) {
    if (Boolean.FALSE.equals(user.getActive())) {
      log.info("User with id '{}' is already not active", user.getId());
    } else {
      user.setActive(false);
      log.info("Updating User with id '{}' with active 'false'. ", user.getId());
      usersClient.updateUser(user.getId(), user);
    }
  }

  private FolioExecutionContext prepareContextForTenant(String tenantId, FolioExecutionContext context) {
    if (MapUtils.isNotEmpty(context.getOkapiHeaders())) {
      context.getOkapiHeaders().put(XOkapiHeaders.TENANT, List.of(tenantId));
      log.info("FOLIO context initialized with tenant {}", tenantId);
    }
    return new DefaultFolioExecutionContext(folioModuleMetadata, context.getOkapiHeaders());
  }

  public void deleteShadowUsers(UUID userId) {
    FolioExecutionContext currentTenantContext = (FolioExecutionContext) folioExecutionContext.getInstance();
    List<UserTenantEntity> userTenantEntities = userTenantRepository.getByUserIdAndIsPrimaryFalse(userId);
    if (CollectionUtils.isNotEmpty(userTenantEntities)) {
      Map<String, List<UserTenantEntity>> listMap = userTenantEntities.stream().collect(Collectors.groupingBy(userTenantEntity -> userTenantEntity.getTenant().getId()));
      log.info("Removing orphaned shadow users from all tenants exist in consortia for the user: {}", userId);
      listMap.forEach((key, value) -> {
        prepareContextForTenant(key, currentTenantContext);
        String id = value.stream().findFirst().get().getUserId().toString();
        usersClient.deleteUsers("?query=(id=(" + id + ")");
        log.info("Removed shadow user: {} from tenant : {}", id, key);
      });
      userTenantRepository.deleteByUserIdAndIsPrimaryFalse(userId);
    }
  }

  private UserTenantEntity toEntity(UserTenant userTenantDto, UUID consortiumId, User user) {
    UserTenantEntity entity = new UserTenantEntity();
    TenantEntity tenant = new TenantEntity();
    tenant.setId(userTenantDto.getTenantId());
    tenant.setName(userTenantDto.getTenantName());
    tenant.setConsortiumId(consortiumId);

    if (Objects.nonNull(userTenantDto.getId())) {
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

  private PermissionUser createPermissionUser(String userId) {
    List<String> emptyPermissionList = new ArrayList<>();
    Optional<PermissionUser> permissionUserOptional = permissionsClient.get("userId==" + userId)
      .getPermissionUsers()
      .stream()
      .findFirst();
    if (permissionUserOptional.isPresent()) {
      // this case possible because for initial admin users setup we are creating user and permissions separately
      log.info("PermissionUser already exist {}.", permissionUserOptional.get());
      return permissionUserOptional.get();
    } else {
      var permissionUser = PermissionUser.of(UUID.randomUUID().toString(), userId, emptyPermissionList);
      log.info("Creating permissionUser {}.", permissionUser);
      return permissionsClient.create(permissionUser);
    }
  }
}
