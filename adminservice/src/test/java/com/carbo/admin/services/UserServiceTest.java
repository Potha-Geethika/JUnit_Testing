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
    private Producer producer;

    @InjectMocks
    private UserService userService;

    private User user;
    private AiUser aiUser;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserName("testuser");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setId("1");

        aiUser = new AiUser();
        aiUser.setAzureUserId("azure123");
        aiUser.setUserName("aiuser");
        aiUser.setName("AI");
        aiUser.setSurname("User");
        aiUser.setEmailAddress("aiuser@example.com");
    }

    @Test
    void getAll() {
        List<User> userList = List.of(user);
        when(userRepository.findAll()).thenReturn(userList);

        List<User> result = userService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUserName());
    }

    @Test
    void getByOrganizationId() {
        String organizationId = "org123";
        when(userRepository.findByOrganizationId(organizationId)).thenReturn(List.of(user));

        List<User> result = userService.getByOrganizationId(organizationId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUser("1");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUserName());
    }

    @Test
    void getUserByUserName() {
        when(userRepository.findByUserNameIgnoreCase("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByUserName("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUserName());
    }

    @Test
    void saveUser() {
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.saveUser(user);

        assertNotNull(result);
        assertEquals("testuser", result.getUserName());
    }

    @Test
    void updateUser() {
        userService.updateUser(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteUser() {
        userService.deleteUser("1");
        verify(userRepository, times(1)).deleteById("1");
    }

    @Test
    void sendOtpEmail_UserNotFound() {
        when(userRepository.findByUserNameIgnoreCase("testuser")).thenReturn(Optional.empty());

        ErrorException exception = assertThrows(ErrorException.class, () -> userService.sendOtpEmail("testuser"));

        assertEquals(USER_NOT_EXISTS_CODE, exception.getError().getErrorCode());
    }

    @Test
    void sendOtpEmail_Success() {
        when(userRepository.findByUserNameIgnoreCase("testuser")).thenReturn(Optional.of(user));
        
        userService.sendOtpEmail("testuser");
        
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void validateOtp_UserNotFound() {
        when(userRepository.findByUserNameIgnoreCase("testuser")).thenReturn(Optional.empty());

        ErrorException exception = assertThrows(ErrorException.class, () -> userService.validateOtp("testuser", "123456", "2022-01-01 10:00:00"));

        assertEquals(USER_NOT_EXISTS_CODE, exception.getError().getErrorCode());
    }

    @Test
    void validateOtp_InvalidOtp() {
        when(userRepository.findByUserNameIgnoreCase("testuser")).thenReturn(Optional.of(user));
        user.setOtpCode("654321");

        ErrorException exception = assertThrows(ErrorException.class, () -> userService.validateOtp("testuser", "123456", "2022-01-01 10:00:00"));

        assertEquals(INVALID_OTP_CODE, exception.getError().getErrorCode());
    }

    @Test
    void validateOtp_Success() {
        when(userRepository.findByUserNameIgnoreCase("testuser")).thenReturn(Optional.of(user));
        user.setOtpCode("123456");
        user.setOtpGeneratedTime("2022-01-01 10:00:00");

        userService.validateOtp("testuser", "123456", "2022-01-01 10:05:00");

        assertNull(user.getOtpCode());
        assertNull(user.getOtpGeneratedTime());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void saveUserOnAzureAd_UserExists() {
        when(userRepository.findByUserNameIgnoreCase("testuser")).thenReturn(Optional.of(user));

        ErrorException exception = assertThrows(ErrorException.class, () -> userService.saveUserOnAzureAd(user));

        assertEquals(USER_ALREADY_EXISTS_CODE, exception.getError().getErrorCode());
    }

    @Test
    void saveUserOnAzureAd_Success() {
        when(userRepository.findByUserNameIgnoreCase("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.saveUserOnAzureAd(user);

        assertEquals(HttpStatus.CREATED, response.getCode());
        assertEquals("User created", response.getMessage());
    }

    @Test
    void updateUserOnAzureAd_UserNotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        ErrorException exception = assertThrows(ErrorException.class, () -> userService.updateUserOnAzureAd(user, "1"));

        assertEquals(USER_NOT_FOUND_MESSAGE, exception.getError().getErrorMessage());
    }

    @Test
    void updateUserOnAzureAd_Success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        user.setPassword("newPassword123");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUserOnAzureAd(user, "1");

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteUserOnAzureAd_UserNotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        ErrorException exception = assertThrows(ErrorException.class, () -> userService.deleteUserOnAzureAd("1"));

        assertEquals(USER_NOT_FOUND_MESSAGE, exception.getError().getErrorMessage());
    }

    @Test
    void deleteUserOnAzureAd_Success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.findByAzureId(anyString())).thenReturn(Optional.of(user));

        userService.deleteUserOnAzureAd("1");

        verify(userRepository, times(1)).deleteById("1");
    }

    @Test
    void updateOpsUserComingFromAi_UserNotFound() {
        when(userRepository.findByAzureId("azure123")).thenReturn(Optional.empty());

        userService.updateOpsUserComingFromAi(aiUser);

        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void updateOpsUserComingFromAi_Success() {
        when(userRepository.findByAzureId("azure123")).thenReturn(Optional.of(user));
        user.setEmailAddress("updated@example.com");

        userService.updateOpsUserComingFromAi(aiUser);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void saveOpsUserComingFromAi_UserExists() {
        when(userRepository.findByAzureId(aiUser.getAzureUserId())).thenReturn(Optional.of(user));

        userService.saveOpsUserComingFromAi(aiUser);

        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void saveOpsUserComingFromAi_Success() {
        when(userRepository.findByAzureId(aiUser.getAzureUserId())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.saveOpsUserComingFromAi(aiUser);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteOpsUserComingFromAi_UserNotFound() {
        when(userRepository.findByAzureId("azure123")).thenReturn(Optional.empty());

        userService.deleteOpsUserComingFromAi("azure123");

        verify(userRepository, times(0)).deleteById(anyString());
    }

    @Test
    void deleteOpsUserComingFromAi_Success() {
        when(userRepository.findByAzureId("azure123")).thenReturn(Optional.of(user));

        userService.deleteOpsUserComingFromAi("azure123");

        verify(userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    void updateLastPasswordResetFlagOnAzure_Success() {
        userService.updateLastPasswordResetFlagOnAzure("azure123");
        // Verify the method call in the GraphServiceClient if it has been mocked
    }

    @Test
    void updateSelectColumn_Success() {
        User user = new User();
        user.setId("1");
        user.setSelectedColumns(new ArrayList<>());
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        userService.updateSelectColumn(new UserPatchDTO("1", List.of("column1", "column2")));

        assertEquals(2, user.getSelectedColumns().size());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUserFilters_Success() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("filter1", "value1");
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        User result = userService.updateUserFilters("1", filters);

        assertNotNull(result);
        assertEquals("1", result.getId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUserFilters_UserNotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        User result = userService.updateUserFilters("1", Collections.emptyMap());

        assertNull(result);
        verify(userRepository, times(0)).save(any(User.class));
    }
}