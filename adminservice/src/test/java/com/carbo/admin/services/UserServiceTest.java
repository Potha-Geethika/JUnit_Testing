package com.carbo.admin.services;
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
    private Producer producer;

    @Mock
    private GraphServiceClient graphServiceClient;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserName("testuser@example.com");
        user.setPassword("Password123!");
    }

    @Test
    void testGetAll() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        assertNotNull(userService.getAll());
        assertEquals(1, userService.getAll().size());
    }

    @Test
    void testGetByOrganizationId() {
        when(userRepository.findByOrganizationId("orgId")).thenReturn(Collections.singletonList(user));
        assertNotNull(userService.getByOrganizationId("orgId"));
        assertEquals(1, userService.getByOrganizationId("orgId").size());
    }

    @Test
    void testGetUser() {
        when(userRepository.findById("userId")).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.getUser("userId");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser@example.com", foundUser.get().getUserName());
    }

    @Test
    void testGetUserByUserName() {
        when(userRepository.findByUserNameIgnoreCase("testuser@example.com")).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.getUserByUserName("testuser@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser@example.com", foundUser.get().getUserName());
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        assertEquals("testuser@example.com", savedUser.getUserName());
    }

    @Test
    void testUpdateUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        userService.updateUser(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById("emailGroupId");
        userService.deleteUser("emailGroupId");
        verify(userRepository, times(1)).deleteById("emailGroupId");
    }

    @Test
    void testSendOtpEmail_UserNotFound() {
        when(userRepository.findByUserNameIgnoreCase("unknownUser")).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.sendOtpEmail("unknownUser"));
        assertEquals("User not exists.", exception.getError().getErrorMessage());
    }

    @Test
    void testSendOtpEmail_Success() {
        when(userRepository.findByUserNameIgnoreCase(user.getUserName())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        userService.sendOtpEmail(user.getUserName());
        assertNotNull(user.getOtpCode());
    }

    @Test
    void testValidateOtp_UserNotFound() {
        when(userRepository.findByUserNameIgnoreCase("unknownUser")).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.validateOtp("unknownUser", "123456", "2023-01-01 00:00:00"));
        assertEquals("User not exists.", exception.getError().getErrorMessage());
    }

    @Test
    void testValidateOtp_InvalidOtp() {
        when(userRepository.findByUserNameIgnoreCase(user.getUserName())).thenReturn(Optional.of(user));
        user.setOtpCode("654321");
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.validateOtp(user.getUserName(), "123456", "2023-01-01 00:00:00"));
        assertEquals("Invalid otp.", exception.getError().getErrorMessage());
    }

    @Test
    void testValidateOtp_OtpExpired() {
        when(userRepository.findByUserNameIgnoreCase(user.getUserName())).thenReturn(Optional.of(user));
        user.setOtpCode("123456");
        user.setOtpGeneratedTime("2023-01-01 00:00:00");
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.validateOtp(user.getUserName(), "123456", "2023-01-01 01:00:00"));
        assertEquals("Otp expired", exception.getError().getErrorMessage());
    }

    @Test
    void testSaveUserOnAzureAd_UserNotFound() {
        when(userRepository.findByUserNameIgnoreCase(user.getUserName())).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.saveUserOnAzureAd(user));
        assertEquals("Username already exists.", exception.getError().getErrorMessage());
    }

    @Test
    void testSaveUserOnAzureAd_Success() {
        when(userRepository.findByUserNameIgnoreCase(user.getUserName())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserResponse response = userService.saveUserOnAzureAd(user);
        assertNotNull(response);
    }

    @Test
    void testUpdateUserOnAzureAd_UserNotFound() {
        when(userRepository.findById("userId")).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.updateUserOnAzureAd(user, "userId"));
        assertEquals("User not found", exception.getError().getErrorMessage());
    }

    @Test
    void testDeleteUserOnAzureAd_UserNotFound() {
        when(userRepository.findById("userId")).thenReturn(Optional.empty());
        ErrorException exception = assertThrows(ErrorException.class, () -> userService.deleteUserOnAzureAd("userId"));
        assertEquals("User not found", exception.getError().getErrorMessage());
    }

    @Test
    void testDeleteUserOnAzureAd_Success() {
        when(userRepository.findById("userId")).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById("userId");
        userService.deleteUserOnAzureAd("userId");
        verify(userRepository, times(1)).deleteById("userId");
    }
}