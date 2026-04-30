package com.carbo.admin.services;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.admin.exception.ErrorException;
import com.carbo.admin.model.Error;
import com.carbo.admin.model.Organization;
import com.carbo.admin.model.Role;
import com.carbo.admin.model.User;
import com.carbo.admin.model.azureB2C.AiUser;
import com.carbo.admin.model.azureB2C.UserResponseDTO;
import com.carbo.admin.repository.UserMongoDbRepository;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.*;
import java.util.stream.Collectors;
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
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;






@ExtendWith(MockitoExtension.class)
class MigrateUsersServiceTest {

    @Mock
    private UserMongoDbRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MigrateUsersService migrateUsersService;

    private List<AiUser> aiUsers;
    private List<User> existingUsers;
    private List<Organization> organizations;

    @BeforeEach
    void setUp() {
        aiUsers = new ArrayList<>();
        existingUsers = new ArrayList<>();
        organizations = new ArrayList<>();
    }

    @Test
    void saveAiUsersAndCollectUnsaved_HappyPath() {
        AiUser aiUser = new AiUser("1", 1, "tenant1", 1, "testUser", "Test", "User", "test@example.com", "1234567890", "ROLE_USER", "notification", "active");
        aiUsers.add(aiUser);

        User existingUser = new User();
        existingUser.setUserName("test@example.com");
        existingUsers.add(existingUser);

        Organization organization = new Organization();
        organization.setId("org1");
        organization.setName("tenant1");
        organizations.add(organization);

        when(userRepository.findAll()).thenReturn(existingUsers);
        when(mongoTemplate.find(any(Query.class), eq(Organization.class))).thenReturn(organizations);
        when(userRepository.saveAll(any())).thenReturn(existingUsers);

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Username already exists in db", result.get(0).getStatus());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_EmptyAiUsers() {
        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(Collections.emptyList());
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository, never()).saveAll(any());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_NoOrganizationFound() {
        AiUser aiUser = new AiUser("1", 1, "unknownTenant", 1, "testUser", "Test", "User", "test@example.com", "1234567890", "ROLE_USER", "notification", "active");
        aiUsers.add(aiUser);

        when(userRepository.findAll()).thenReturn(existingUsers);
        when(mongoTemplate.find(any(Query.class), eq(Organization.class))).thenReturn(Collections.emptyList());

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Organization is not found with the tenant name", result.get(0).getStatus());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_ExceptionHandling() {
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        ErrorException exception = assertThrows(ErrorException.class, () -> {
            migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);
        });

        assertEquals("Some Error occurred : Database error", exception.getError().getErrorMessage());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), exception.getError().getErrorCode());
    }

    @Test
    void getAllUsersForAi_HappyPath() {
        List<UserResponseDTO> userResponseList = new ArrayList<>();
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setAzureId("azureId1");
        userResponseDTO.setOrganizationId("org1");
        userResponseList.add(userResponseDTO);

        Organization organization = new Organization();
        organization.setId("org1");
        organization.setName("Organization 1");
        organizations.add(organization);

        when(userRepository.findAllUsersWithSelectedFields()).thenReturn(userResponseList);
        when(mongoTemplate.findAll(Organization.class)).thenReturn(organizations);

        List<UserResponseDTO> result = migrateUsersService.getAllUsersForAi();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Organization 1", result.get(0).getOrganizationName());
    }

    @Test
    void getAllUsersForAi_NoUsers() {
        when(userRepository.findAllUsersWithSelectedFields()).thenReturn(Collections.emptyList());
        when(mongoTemplate.findAll(Organization.class)).thenReturn(Collections.emptyList());

        List<UserResponseDTO> result = migrateUsersService.getAllUsersForAi();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void setUserAzureId_HappyPath() {
        AiUser aiUser = new AiUser("1", 1, "tenant1", 1, "testUser", "Test", "User", "test@example.com", "1234567890", "ROLE_USER", "notification", "active");
        List<AiUser> users = Collections.singletonList(aiUser);

        User user = new User();
        user.setUserName("test@example.com");
        user.setAzureId("oldAzureId");
        existingUsers.add(user);

        when(userRepository.findAll()).thenReturn(existingUsers);
        when(userRepository.save(any())).thenReturn(user);

        List<User> result = migrateUsersService.setUserAzureId(users);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getAzureId());
    }

    @Test
    void setUserAzureId_NoMatch() {
        AiUser aiUser = new AiUser("1", 1, "tenant1", 1, "testUser", "Test", "User", "nonexistent@example.com", "1234567890", "ROLE_USER", "notification", "active");
        List<AiUser> users = Collections.singletonList(aiUser);

        when(userRepository.findAll()).thenReturn(existingUsers);

        List<User> result = migrateUsersService.setUserAzureId(users);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository, never()).save(any());
    }

    @Test
    void setUserAzureId_ExceptionHandling() {
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        ErrorException exception = assertThrows(ErrorException.class, () -> {
            migrateUsersService.setUserAzureId(aiUsers);
        });

        assertEquals("Some Error occurred : Database error", exception.getError().getErrorMessage());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), exception.getError().getErrorCode());
    }
}