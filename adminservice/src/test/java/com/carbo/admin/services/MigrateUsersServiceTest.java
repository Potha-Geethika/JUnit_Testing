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
        AiUser aiUser = new AiUser("123", 1, "Tenant1", 1, "user1", "First", "Last", "user1@example.com", "1234567890", "USER_ROLE", "notificationType", "ACTIVE");
        aiUsers.add(aiUser);
        
        existingUsers.add(new User());
        when(userRepository.findAll()).thenReturn(existingUsers);

        Organization organization = new Organization();
        organization.setId("org1");
        organization.setName("Tenant1");
        organizations.add(organization);
        when(mongoTemplate.find(any(Query.class), eq(Organization.class))).thenReturn(organizations);

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository).saveAll(anyList());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_OrganizationNotFound() {
        AiUser aiUser = new AiUser("123", 1, "Tenant1", 1, "user1", "First", "Last", "user1@example.com", "1234567890", "USER_ROLE", "notificationType", "ACTIVE");
        aiUsers.add(aiUser);
        
        existingUsers.add(new User());
        when(userRepository.findAll()).thenReturn(existingUsers);
        when(mongoTemplate.find(any(Query.class), eq(Organization.class))).thenReturn(Collections.emptyList());

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Organization is not found with the tenant name", result.get(0).getStatus());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_UsernameExists() {
        AiUser aiUser = new AiUser("123", 1, "Tenant1", 1, "user1", "First", "Last", "user1@example.com", "1234567890", "USER_ROLE", "notificationType", "ACTIVE");
        aiUsers.add(aiUser);
        
        User existingUser = new User();
        existingUser.setUserName("user1@example.com");
        existingUsers.add(existingUser);
        when(userRepository.findAll()).thenReturn(existingUsers);

        Organization organization = new Organization();
        organization.setId("org1");
        organization.setName("Tenant1");
        organizations.add(organization);
        when(mongoTemplate.find(any(Query.class), eq(Organization.class))).thenReturn(organizations);

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Username already exists in db", result.get(0).getStatus());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_ExceptionHandling() {
        AiUser aiUser = new AiUser("123", 1, "Tenant1", 1, "user1", "First", "Last", "user1@example.com", "1234567890", "USER_ROLE", "notificationType", "ACTIVE");
        aiUsers.add(aiUser);
        
        when(userRepository.findAll()).thenThrow(new RuntimeException("DB Error"));

        Exception exception = assertThrows(ErrorException.class, () -> migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers));

        assertNotNull(exception);
        assertEquals("Some Error occurred : DB Error", ((ErrorException) exception).getError().getErrorMessage());
    }

    @Test
    void getAllUsersForAi_HappyPath() {
        List<UserResponseDTO> userResponseDTOS = new ArrayList<>();
        when(userRepository.findAllUsersWithSelectedFields()).thenReturn(userResponseDTOS);
        when(mongoTemplate.findAll(Organization.class)).thenReturn(organizations);

        List<UserResponseDTO> result = migrateUsersService.getAllUsersForAi();

        assertNotNull(result);
        assertEquals(userResponseDTOS, result);
    }

    @Test
    void setUserAzureId_HappyPath() {
        AiUser aiUser = new AiUser("123", 1, "Tenant1", 1, "user1", "First", "Last", "user1@example.com", "1234567890", "USER_ROLE", "notificationType", "ACTIVE");
        List<AiUser> users = Collections.singletonList(aiUser);
        User retrievedUser = new User();
        retrievedUser.setUserName("user1@example.com");
        retrievedUser.setAzureId("azure-123");
        existingUsers.add(retrievedUser);
        when(userRepository.findAll()).thenReturn(existingUsers);
        when(userRepository.save(any(User.class))).thenReturn(retrievedUser);

        List<User> result = migrateUsersService.setUserAzureId(users);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("azure-123", result.get(0).getAzureId());
        verify(userRepository).save(retrievedUser);
    }

    @Test
    void setUserAzureId_ExceptionHandling() {
        AiUser aiUser = new AiUser("123", 1, "Tenant1", 1, "user1", "First", "Last", "user1@example.com", "1234567890", "USER_ROLE", "notificationType", "ACTIVE");
        List<AiUser> users = Collections.singletonList(aiUser);
        when(userRepository.findAll()).thenThrow(new RuntimeException("DB Error"));

        Exception exception = assertThrows(ErrorException.class, () -> migrateUsersService.setUserAzureId(users));

        assertNotNull(exception);
        assertEquals("Some Error occurred : DB Error", ((ErrorException) exception).getError().getErrorMessage());
    }
}