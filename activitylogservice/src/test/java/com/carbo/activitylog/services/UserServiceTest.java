package com.carbo.activitylog.services;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.activitylog.model.User;
import com.carbo.activitylog.repository.UserMongoDbRepository;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;






@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMongoDbRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("1");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("john.doe");
        user.setPassword("password");
        user.setTitle("Developer");
        user.setOrganizationId("org-123");
    }

    @Test
    void testGetAll() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<User> users = userService.getAll();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("John", users.get(0).getFirstName());
    }

    @Test
    void testGetByOrganizationId() {
        when(userRepository.findByOrganizationId(anyString())).thenReturn(Collections.singletonList(user));

        List<User> users = userService.getByOrganizationId("org-123");

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("John", users.get(0).getFirstName());
    }

    @Test
    void testGetUser() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUser("1");

        assertTrue(foundUser.isPresent());
        assertEquals("John", foundUser.get().getFirstName());
    }

    @Test
    void testGetUserByUserName() {
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUserByUserName("john.doe");

        assertTrue(foundUser.isPresent());
        assertEquals("John", foundUser.get().getFirstName());
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.saveUser(user);

        assertNotNull(savedUser);
        assertEquals("John", savedUser.getFirstName());
    }

    @Test
    void testUpdateUser() {
        doNothing().when(userRepository).save(any(User.class));

        assertDoesNotThrow(() -> userService.updateUser(user));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(anyString());

        assertDoesNotThrow(() -> userService.deleteUser("1"));
        verify(userRepository, times(1)).deleteById("1");
    }

    @Test
    void testGetAllEmpty() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> users = userService.getAll();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testGetByOrganizationIdNotFound() {
        when(userRepository.findByOrganizationId(anyString())).thenReturn(Collections.emptyList());

        List<User> users = userService.getByOrganizationId("non-existing-org");

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testGetUserNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUser("non-existing-id");

        assertFalse(foundUser.isPresent());
    }

    @Test
    void testGetUserByUserNameNotFound() {
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserByUserName("non-existing-username");

        assertFalse(foundUser.isPresent());
    }
}