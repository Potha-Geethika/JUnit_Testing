package com.carbo.activitylog.services;
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

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setUserName("johndoe");
        testUser.setPassword("password");
        testUser.setTitle("Developer");
        testUser.setOrganizationId("org-1");
    }

    @Test
    void testGetAll() {
        List<User> expectedUsers = Collections.singletonList(testUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> users = userService.getAll();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("John", users.get(0).getFirstName());
    }

    @Test
    void testGetByOrganizationId() {
        List<User> expectedUsers = Collections.singletonList(testUser);
        when(userRepository.findByOrganizationId("org-1")).thenReturn(expectedUsers);

        List<User> users = userService.getByOrganizationId("org-1");

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("John", users.get(0).getFirstName());
    }

    @Test
    void testGetUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        Optional<User> user = userService.getUser("1");

        assertNotNull(user);
        assertEquals("John", user.get().getFirstName());
    }

    @Test
    void testGetUserByUserName() {
        when(userRepository.findByUserName("johndoe")).thenReturn(Optional.of(testUser));

        Optional<User> user = userService.getUserByUserName("johndoe");

        assertNotNull(user);
        assertEquals("John", user.get().getFirstName());
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User savedUser = userService.saveUser(testUser);

        assertNotNull(savedUser);
        assertEquals("John", savedUser.getFirstName());
    }

    @Test
    void testUpdateUser() {
        doNothing().when(userRepository).save(testUser);

        userService.updateUser(testUser);

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById("1");

        userService.deleteUser("1");

        verify(userRepository, times(1)).deleteById("1");
    }
}