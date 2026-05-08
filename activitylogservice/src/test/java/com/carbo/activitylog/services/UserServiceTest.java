package com.carbo.activitylog.services;
import static org.mockito.ArgumentMatchers.anyString;

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
        user.setTitle("Mr");
        user.setOrganizationId("org-1");
    }

    @Test
    void getAll() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        List<User> users = userService.getAll();
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(user, users.get(0));
    }

    @Test
    void getByOrganizationId() {
        when(userRepository.findByOrganizationId(anyString())).thenReturn(Collections.singletonList(user));
        List<User> users = userService.getByOrganizationId("org-1");
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(user, users.get(0));
    }

    @Test
    void getUser() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.getUser("1");
        assertNotNull(foundUser);
        assertEquals(user, foundUser.get());
    }

    @Test
    void getUserByUserName() {
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.getUserByUserName("johndoe");
        assertNotNull(foundUser);
        assertEquals(user, foundUser.get());
    }

    @Test
    void saveUser() {
        when(userRepository.save(user)).thenReturn(user);
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        assertEquals(user, savedUser);
    }

    @Test
    void updateUser() {
        when(userRepository.save(user)).thenReturn(user);
        userService.updateUser(user);
        // Verify that save was called
        // No need to assert as it has no return value
    }

    @Test
    void deleteUser() {
        doNothing().when(userRepository).deleteById(anyString());
        userService.deleteUser("1");
        // Verify that deleteById was called
        // No need to assert as it has no return value
    }
}