package org.folio.consortia.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.folio.consortia.client.UsersClient;
import org.folio.consortia.config.FolioExecutionContextHelper;
import org.folio.consortia.domain.dto.Personal;
import org.folio.consortia.domain.dto.User;
import org.folio.consortia.exception.ResourceNotFoundException;
import org.folio.consortia.service.impl.UserServiceImpl;
import org.folio.spring.FolioExecutionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
class UserServiceTest {

  @InjectMocks
  UserServiceImpl userService;
  @Mock
  UsersClient usersClient;
  @Mock
  FolioExecutionContext folioExecutionContext = new FolioExecutionContext() {
  };
  @Mock
  FolioExecutionContextHelper contextHelper;

  @Test
  void shouldCreateUser() {
    User user = createUserEntity(false);
    Mockito.doNothing()
      .when(usersClient)
      .saveUser(user);
    User createdUser = userService.createUser(user);
    Assertions.assertEquals(user, createdUser);
  }

  @Test
  void shouldUpdateUser() {
    User user = createUserEntity(false);
    Mockito.doNothing()
      .when(usersClient)
      .updateUser(user.getId(), user);
    Assertions.assertDoesNotThrow(() -> userService.updateUser(user));
  }

  @Test
  void shouldDeleteUser() {
    User user = createUserEntity(false);
    Mockito.doNothing()
      .when(usersClient)
      .deleteUser(user.getId());
    Assertions.assertDoesNotThrow(() -> userService.deleteById(user.getId()));
  }

  @Test
  void shouldThrowNotFoundWhilePrepareShadowUser() {
    when(folioExecutionContext.getTenantId()).thenReturn("diku");
    doReturn(folioExecutionContext).when(contextHelper)
      .getFolioExecutionContext(anyString());

    Mockito.when(usersClient.getUsersByUserId(any()))
      .thenReturn(new User());
    Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.prepareShadowUser(UUID.randomUUID(), ""));
  }

  @Test
  void shouldPrepareShadowUser() {
    when(folioExecutionContext.getTenantId()).thenReturn("diku");
    doReturn(folioExecutionContext).when(contextHelper)
      .getFolioExecutionContext(anyString());

    Mockito.when(usersClient.getUsersByUserId(any()))
      .thenReturn(createUserEntity(true));
    User user = userService.prepareShadowUser(UUID.randomUUID(), "diku");
    Assertions.assertEquals(true, user.getActive());
  }

  private User createUserEntity(Boolean updateble) {
    User user = new User();
    Personal personal = new Personal();
    personal.setPreferredContactTypeId("email");
    personal.setEmail("Test@mail.com");
    personal.setFirstName("testFirst");
    personal.setLastName("testLast");
    user.setId(UUID.randomUUID()
      .toString());
    user.setPatronGroup(null);
    user.setUsername("xyz");
    user.setPersonal(personal);
    user.setActive(Boolean.FALSE.equals(updateble));

    return user;
  }
}
