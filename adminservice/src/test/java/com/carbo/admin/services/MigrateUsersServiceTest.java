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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveAiUsersAndCollectUnsaved_HappyPath() {
        List<AiUser> aiUsers = new ArrayList<>();
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("test@example.com");
        aiUser.setTenantName("TestOrg");
        aiUsers.add(aiUser);

        User existingUser = new User();
        existingUser.setUserName("test@example.com");
        when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));

        Organization organization = new Organization();
        organization.setId("orgId");
        organization.setName("TestOrg");
        when(mongoTemplate.find(any(Query.class), eq(Organization.class))).thenReturn(Collections.singletonList(organization));

        when(userRepository.saveAll(any())).thenReturn(Collections.emptyList());

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Username already exists in db", result.get(0).getStatus());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_OrganizationNotFound() {
        List<AiUser> aiUsers = new ArrayList<>();
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("newuser@example.com");
        aiUser.setTenantName("NonExistentOrg");
        aiUsers.add(aiUser);

        User existingUser = new User();
        existingUser.setUserName("existing@example.com");
        when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));

        when(mongoTemplate.find(any(Query.class), eq(Organization.class))).thenReturn(Collections.emptyList());

        List<AiUser> result = migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User not created : Organization is not found with the tenant name", result.get(0).getStatus());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_ExceptionThrown() {
        List<AiUser> aiUsers = new ArrayList<>();
        AiUser aiUser = new AiUser();
        aiUsers.add(aiUser);

        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        ErrorException thrown = org.junit.jupiter.api.Assertions.assertThrows(ErrorException.class, () -> {
            migrateUsersService.saveAiUsersAndCollectUnsaved(aiUsers);
        });

        assertNotNull(thrown);
        assertEquals(HttpStatus.BAD_REQUEST.toString(), thrown.getError().getErrorCode());
        assertEquals("Some Error occurred : Database error", thrown.getError().getErrorMessage());
    }

    @Test
    void getAllUsersForAi_HappyPath() {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setOrganizationId("orgId");

        Organization organization = new Organization();
        organization.setId("orgId");
        organization.setName("TestOrg");

        when(userRepository.findAllUsersWithSelectedFields()).thenReturn(Collections.singletonList(userResponseDTO));
        when(mongoTemplate.findAll(Organization.class)).thenReturn(Collections.singletonList(organization));

        List<UserResponseDTO> result = migrateUsersService.getAllUsersForAi();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TestOrg", result.get(0).getOrganizationName());
    }

    @Test
    void setUserAzureId_HappyPath() {
        List<AiUser> aiUsers = new ArrayList<>();
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("test@example.com");
        aiUser.setAzureUserId("azureId");
        aiUsers.add(aiUser);

        User user = new User();
        user.setUserName("test@example.com");
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(userRepository.save(any())).thenReturn(user);

        List<User> result = migrateUsersService.setUserAzureId(aiUsers);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("azureId", result.get(0).getAzureId());
    }

    @Test
    void setUserAzureId_ExceptionThrown() {
        List<AiUser> aiUsers = new ArrayList<>();
        AiUser aiUser = new AiUser();
        aiUsers.add(aiUser);

        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        ErrorException thrown = org.junit.jupiter.api.Assertions.assertThrows(ErrorException.class, () -> {
            migrateUsersService.setUserAzureId(aiUsers);
        });

        assertNotNull(thrown);
        assertEquals(HttpStatus.BAD_REQUEST.toString(), thrown.getError().getErrorCode());
        assertEquals("Some Error occurred : Database error", thrown.getError().getErrorMessage());
    }
}