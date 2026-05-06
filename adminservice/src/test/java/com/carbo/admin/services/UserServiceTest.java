package com.carbo.admin.services;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.admin.exception.ErrorException;
import com.carbo.admin.kafka.Producer;
import com.carbo.admin.model.*;
import com.carbo.admin.model.Error;
import com.carbo.admin.model.azureB2C.AiUser;
import com.carbo.admin.model.azureB2C.UserResponse;
import com.carbo.admin.repository.UserMongoDbRepository;
import com.carbo.admin.utils.Constants;
import com.microsoft.graph.models.ObjectIdentity;
import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import static com.carbo.admin.utils.Constants.INVALID_OTP_CODE;
import static com.carbo.admin.utils.Constants.OTP_EXPIRED_CODE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;






@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserMongoDbRepository userRepository;
    
    @Mock
    private MongoTemplate mongoTemplate;
    
    @Mock
    private Producer producer;
    
    @Mock
    private GraphServiceClient graphServiceClient;
    
    @InjectMocks
    private UserService userService;

    private User testUser;
    private AiUser testAiUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserName("testuser");
        testUser.setPassword("Password1!");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        
        testAiUser = new AiUser();
        testAiUser.setAzureUserId("azure-test-id");
        testAiUser.setUserName("testuser");
        testAiUser.setName("Test");
        testAiUser.setSurname("User");
        testAiUser.setEmailAddress("testuser@example.com");
    }

    @Test
    void getAll() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        assertNotNull(userService.getAll());
        assertEquals(1, userService.getAll().size());
    }

    @Test
    void getByOrganizationId() {
        String organizationId = "org-id";
        when(userRepository.findByOrganizationId(organizationId)).thenReturn(Collections.singletonList(testUser));
        assertNotNull(userService.getByOrganizationId(organizationId));
        assertEquals(1, userService.getByOrganizationId(organizationId).size());
    }

    @Test
    void getUser() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        assertTrue(userService.getUser("test-id").isPresent());
    }

    @Test
    void getUserByUserName() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        assertTrue(userService.getUserByUserName("testuser").isPresent());
    }

    @Test
    void saveUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        User savedUser = userService.saveUser(testUser);
        assertNotNull(savedUser);
        assertEquals(testUser.getUserName(), savedUser.getUserName());
    }

    @Test
    void updateUser() {
        doNothing().when(userRepository).save(any(User.class));
        userService.updateUser(testUser);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void deleteUser() {
        doNothing().when(userRepository).deleteById(anyString());
        userService.deleteUser("test-email");
        verify(userRepository, times(1)).deleteById("test-email");
    }

    @Test
    void sendOtpEmail_UserNotFound() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.sendOtpEmail("nonexistent"));
        assertEquals(Constants.USER_NOT_EXISTS_CODE, exception.getError().getErrorCode());
    }

    @Test
    void sendOtpEmail_Success() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).save(any(User.class));
        userService.sendOtpEmail(testUser.getUserName());
        assertNotNull(testUser.getOtpCode());
    }

    @Test
    void validateOtp_UserNotFound() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.validateOtp("nonexistent", "123456", "2023-01-01 00:00:00"));
        assertEquals(Constants.USER_NOT_EXISTS_CODE, exception.getError().getErrorCode());
    }

    @Test
    void validateOtp_Success() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        testUser.setOtpCode("123456");
        userService.validateOtp(testUser.getUserName(), "123456", "2023-01-01 00:00:00");
        assertNull(testUser.getOtpCode());
    }

    @Test
    void saveUserOnAzureAd_UserAlreadyExists() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.of(testUser));
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.saveUserOnAzureAd(testUser));
        assertEquals(Constants.USER_ALREADY_EXISTS_CODE, exception.getError().getErrorCode());
    }

    @Test
    void saveUserOnAzureAd_Success() {
        when(userRepository.findByUserNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(graphServiceClient.users().post(any())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        UserResponse response = userService.saveUserOnAzureAd(testUser);
        assertEquals(HttpStatus.CREATED, response.getCode());
    }

    @Test
    void updateUserOnAzureAd_UserNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.updateUserOnAzureAd(testUser, "nonexistent"));
        assertEquals(Constants.USER_NOT_FOUND_MESSAGE, exception.getError().getErrorMessage());
    }

    @Test
    void updateUserOnAzureAd_Success() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).save(any(User.class));
        userService.updateUserOnAzureAd(testUser, "existing-id");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void deleteUserOnAzureAd_UserNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.deleteUserOnAzureAd("nonexistent"));
        assertEquals(Constants.USER_NOT_FOUND_MESSAGE, exception.getError().getErrorMessage());
    }

    @Test
    void deleteUserOnAzureAd_Success() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(anyString());
        userService.deleteUserOnAzureAd("existing-id");
        verify(userRepository, times(1)).deleteById("existing-id");
    }

    @Test
    void saveOpsUserComingFromAi_UserAlreadyExists() {
        when(userRepository.findByAzureId(anyString())).thenReturn(Optional.of(testUser));
        userService.saveOpsUserComingFromAi(testAiUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void saveOpsUserComingFromAi_Success() {
        when(userRepository.findByAzureId(anyString())).thenReturn(Optional.empty());
        when(mongoTemplate.findOne(any(), eq(User.class))).thenReturn(testUser);
        userService.saveOpsUserComingFromAi(testAiUser);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateOpsUserComingFromAi_UserNotFound() {
        when(userRepository.findByAzureId(anyString())).thenReturn(Optional.empty());
        userService.updateOpsUserComingFromAi(testAiUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateOpsUserComingFromAi_Success() {
        when(userRepository.findByAzureId(anyString())).thenReturn(Optional.of(testUser));
        userService.updateOpsUserComingFromAi(testAiUser);
        verify(userRepository, times(1)).save(testUser);
    }
}