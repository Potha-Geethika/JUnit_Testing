package com.carbo.admin.controllers;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.admin.exception.ErrorException;
import com.carbo.admin.model.*;
import com.carbo.admin.model.Error;
import com.carbo.admin.services.DistrictService;
import com.carbo.admin.services.UserService;
import com.carbo.admin.utils.Constants;
import com.carbo.admin.utils.ControllerUtil;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.mongodb.MongoWriteException;
import io.netty.handler.ssl.SslContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
import java.time.Instant;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import static com.carbo.admin.utils.Constants.*;
import static com.carbo.admin.utils.Constants.USER_UPDATE_MESSAGE;
import static com.carbo.admin.utils.ControllerUtil.getUserName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.passay.CharacterCharacteristicsRule.ERROR_CODE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;






@WebMvcTest(UserServiceController.class)
class UserServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private DistrictService districtService;

    private UserServiceController userServiceController;

    @BeforeEach
    void setUp() {
        userServiceController = new UserServiceController(userService, districtService, null, null);
    }

    @Test
    void getUsers_HappyPath() throws Exception {
        User user = new User();
        user.setUserName("test@example.com");
        when(userService.getAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/v1/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("test@example.com"));

        verify(userService).getAll();
    }

    @Test
    void getUsers_ThrowsNullPointerException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(() -> "duy.nguyen@carboceramics.com");

        when(userService.getByOrganizationId(anyString())).thenThrow(new NullPointerException());

        mockMvc.perform(get("/v1/users/").requestAttr("orgId", "testOrg"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getAll();
    }

    @Test
    void getUser_HappyPath() throws Exception {
        User user = new User();
        user.setUserName("test@example.com");
        when(userService.getUser("1")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("test@example.com"));

        verify(userService).getUser("1");
    }

    @Test
    void getUser_NotFound() throws Exception {
        when(userService.getUser("1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/users/1"))
                .andExpect(status().isNotFound());

        verify(userService).getUser("1");
    }

    @Test
    void getUserLastPassResetDate_HappyPath() throws Exception {
        User user = new User();
        user.setLastPassResetDate(new Date());
        when(userService.getUserByUserName("testUser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/v1/users/lastPassResetDate/testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());

        verify(userService).getUserByUserName("testUser");
    }

    @Test
    void getUserLastPassResetDate_NotFound() throws Exception {
        when(userService.getUserByUserName("testUser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/users/lastPassResetDate/testUser"))
                .andExpect(status().isNotFound());

        verify(userService).getUserByUserName("testUser");
    }

    @Test
    void updateLastPasswordResetDate_HappyPath() throws Exception {
        User user = new User();
        user.setUserName("testUser");
        user.setLastPassResetDate(new Date());
        when(userService.getUserByUserName("testUser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/v1/users/lastPassResetDate/testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastPassResetDate\":\"2023-01-01T00:00:00Z\"}"))
                .andExpect(status().isOk());

        verify(userService).saveUser(any(User.class));
    }

    @Test
    void updateLastPasswordResetDate_UserNotFound() throws Exception {
        when(userService.getUserByUserName("testUser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/v1/users/lastPassResetDate/testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastPassResetDate\":\"2023-01-01T00:00:00Z\"}"))
                .andExpect(status().isNotFound());

        verify(userService).getUserByUserName("testUser");
    }

    @Test
    void updateUser_HappyPath() throws Exception {
        User existingUser = new User();
        existingUser.setUserName("testUser");
        when(userService.getUser("1")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(put("/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"testUserUpdated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successMessage").value("User updated Successfully"));

        verify(userService).updateUserOnAzureAd(any(User.class), anyString());
    }

    @Test
    void updateUser_UserNotFound() throws Exception {
        when(userService.getUser("1")).thenReturn(Optional.empty());

        mockMvc.perform(put("/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"testUserUpdated\"}"))
                .andExpect(status().isNotFound());

        verify(userService).getUser("1");
    }

    @Test
    void saveUser_HappyPath() throws Exception {
        User user = new User();
        user.setUserName("newUser@example.com");

        mockMvc.perform(post("/v1/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"newUser@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successMessage").value("User created Successfully"));

        verify(userService).saveUserOnAzureAd(any(User.class));
    }

    @Test
    void saveUser_UserAlreadyExists() throws Exception {
        doThrow(new DuplicateKeyException("User exists")).when(userService).saveUserOnAzureAd(any(User.class));

        mockMvc.perform(post("/v1/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"existingUser@example.com\"}"))
                .andExpect(status().isBadRequest());

        verify(userService).saveUserOnAzureAd(any(User.class));
    }

    @Test
    void deleteUser_HappyPath() throws Exception {
        doNothing().when(userService).deleteUserOnAzureAd("1");

        mockMvc.perform(delete("/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserOnAzureAd("1");
    }

    @Test
    void deleteUser_UserNotFound() throws Exception {
        doThrow(new ErrorException(any())).when(userService).deleteUserOnAzureAd("1");

        mockMvc.perform(delete("/v1/users/1"))
                .andExpect(status().isNotFound());

        verify(userService).deleteUserOnAzureAd("1");
    }

    @Test
    void changePassword_HappyPath() throws Exception {
        User existingUser = new User();
        existingUser.setUserName("testUser");
        existingUser.setPassword("oldPassword");
        when(userService.getUserByUserName("testUser")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(put("/v1/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"curPassword\":\"oldPassword\",\"newPassword\":\"newPassword\"}"))
                .andExpect(status().isOk());

        verify(userService).updateUser(any(User.class));
    }

    @Test
    void changePassword_CurrentPasswordNotMatch() throws Exception {
        User existingUser = new User();
        existingUser.setUserName("testUser");
        existingUser.setPassword("oldPassword");
        when(userService.getUserByUserName("testUser")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(put("/v1/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"curPassword\":\"wrongPassword\",\"newPassword\":\"newPassword\"}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(any(User.class));
    }

    @Test
    void changePassword_UserNotFound() throws Exception {
        when(userService.getUserByUserName("testUser")).thenReturn(Optional.empty());

        mockMvc.perform(put("/v1/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"curPassword\":\"oldPassword\",\"newPassword\":\"newPassword\"}"))
                .andExpect(status().isNotFound());

        verify(userService).getUserByUserName("testUser");
    }
}