package com.carbo.activitylog.services;

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
        user.setUserName("johndoe");
        user.setPassword("password");
        user.setTitle("Mr.");
        user.setOrganizationId("org-1");
    }

    @Test
    void testGetAll() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        List<User> users = userService.getAll();
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(user, users.get(0));
    }

    @Test
    void testGetByOrganizationId() {
        when(userRepository.findByOrganizationId("org-1")).thenReturn(Collections.singletonList(user));
        List<User> users = userService.getByOrganizationId("org-1");
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(user, users.get(0));
    }

    @Test
    void testGetUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        Optional<User> retrievedUser = userService.getUser("1");
        assertTrue(retrievedUser.isPresent());
        assertEquals(user, retrievedUser.get());
    }

    @Test
    void testGetUserByUserName() {
        when(userRepository.findByUserName("johndoe")).thenReturn(Optional.of(user));
        Optional<User> retrievedUser = userService.getUserByUserName("johndoe");
        assertTrue(retrievedUser.isPresent());
        assertEquals(user, retrievedUser.get());
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(user)).thenReturn(user);
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        assertEquals(user, savedUser);
    }

    @Test
    void testUpdateUser() {
        when(userRepository.save(user)).thenReturn(user);
        userService.updateUser(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser("1");
        verify(userRepository, times(1)).deleteById("1");
    }

    @Test
    void testGetByOrganizationId_emptyList() {
        when(userRepository.findByOrganizationId("unknown-org")).thenReturn(Collections.emptyList());
        List<User> users = userService.getByOrganizationId("unknown-org");
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testGetUser_notFound() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());
        Optional<User> retrievedUser = userService.getUser("unknown");
        assertFalse(retrievedUser.isPresent());
    }

    @Test
    void testGetUserByUserName_notFound() {
        when(userRepository.findByUserName("unknown")).thenReturn(Optional.empty());
        Optional<User> retrievedUser = userService.getUserByUserName("unknown");
        assertFalse(retrievedUser.isPresent());
    }
}