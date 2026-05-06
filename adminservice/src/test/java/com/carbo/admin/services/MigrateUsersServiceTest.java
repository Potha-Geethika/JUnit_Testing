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

    @BeforeEach
    void setUp() {
        aiUsers = new ArrayList<>();
        existingUsers = new ArrayList<>();
    }

    @Test
    void testSaveAiUsersAndCollectUnsaved_HappyPath() {
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("test@example.com");
        aiUser.setTenantName("TestOrg");
        aiUsers.add(aiUser);

        User existingUser = new User();
        existingUser.setUserName("test@example.com");
        existingUsers.add(existingUser);

        when(userRepository.findAll()).thenReturn(existingUsers);
        when(mongoTemplate.find(any(Query.class), eq(Organization.class))).thenReturn(Collections.emptyList());

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Username already exists in db", result.get(0).getStatus());
    }

    @Test
    void testSaveAiUsersAndCollectUnsaved_OrganizationNotFound() {
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("newuser@example.com");
        aiUser.setTenantName("NonExistentOrg");
        aiUsers.add(aiUser);

        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(mongoTemplate.find(any(Query.class), eq(Organization.class))).thenReturn(Collections.emptyList());

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Organization is not found with the tenant name", result.get(0).getStatus());
    }

    @Test
    void testSaveAiUsersAndCollectUnsaved_ExceptionHandling() {
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("newuser@example.com");
        aiUsers.add(aiUser);

        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(ErrorException.class, () -> {
            migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);
        });

        assertEquals("Some Error occurred : Database error", exception.getMessage());
    }

    @Test
    void testGetAllUsersForAi() {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setOrganizationId("orgId");
        List<UserResponseDTO> userResponseDTOS = Collections.singletonList(userResponseDTO);
        when(userRepository.findAllUsersWithSelectedFields()).thenReturn(userResponseDTOS);

        Organization organization = new Organization();
        organization.setId("orgId");
        organization.setName("Test Organization");
        when(mongoTemplate.findAll(Organization.class)).thenReturn(Collections.singletonList(organization));

        List<UserResponseDTO> result = migrateUsersService.getAllUsersForAi();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Organization", result.get(0).getOrganizationName());
    }

    @Test
    void testSetUserAzureId_HappyPath() {
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("user@example.com");
        aiUser.setAzureUserId("azure-id-123");
        List<AiUser> aiUsers = Collections.singletonList(aiUser);

        User user = new User();
        user.setUserName("user@example.com");
        user.setAzureId("old-azure-id");
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        List<User> result = migrateUsersService.setUserAzureId(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("azure-id-123", result.get(0).getAzureId());
        verify(userRepository).save(user);
    }

    @Test
    void testSetUserAzureId_UserNotFound() {
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("nonexistent@example.com");
        aiUser.setAzureUserId("azure-id-123");
        List<AiUser> aiUsers = Collections.singletonList(aiUser);

        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> result = migrateUsersService.setUserAzureId(aiUsers);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSetUserAzureId_ExceptionHandling() {
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("user@example.com");
        List<AiUser> aiUsers = Collections.singletonList(aiUser);

        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(ErrorException.class, () -> {
            migrateUsersService.setUserAzureId(aiUsers);
        });

        assertEquals("Some Error occurred : Database error", exception.getMessage());
    }

    @Test
    void testConvertAiUserToOpsUser() {
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("user@example.com");
        aiUser.setName("John");
        aiUser.setSurname("Doe");
        
        User user = migrateUsersService.convertAiUserToOpsUser(aiUser);

        assertNotNull(user);
        assertEquals("user@example.com", user.getUserName());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("CreatedBy-AIUser", user.getCreatedBy());
        assertEquals("ModifiedBy-AIUser", user.getLastModifiedBy());
        assertEquals("Default-AIUser", user.getTitle());
        assertEquals("Active", user.getStatus());
    }
}