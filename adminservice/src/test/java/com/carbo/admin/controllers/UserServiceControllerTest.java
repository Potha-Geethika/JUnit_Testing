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

    @Mock
    private UserService userService;

    @Mock
    private DistrictService districtService;

    @InjectMocks
    private UserServiceController userServiceController;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setUserName("testUser");
        testUser.setLastPassResetDate(new Date());
        testUser.setStrength(true);
    }

    @Test
    @DisplayName("Get all users - happy path")
    void getUsers_HappyPath() throws Exception {
        when(userService.getAll()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/v1/users/").principal(mock(Principal.class)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("testUser"));

        verify(userService, times(1)).getAll();
    }

    @Test
    @DisplayName("Get user - happy path")
    void getUser_HappyPath() throws Exception {
        when(userService.getUser(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/v1/users/{userId}", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("testUser"));

        verify(userService, times(1)).getUser("1");
    }

    @Test
    @DisplayName("Get user - 404 Not Found")
    void getUser_NotFound() throws Exception {
        when(userService.getUser(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/users/{userId}", "1"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUser("1");
    }

    @Test
    @DisplayName("Save last password reset date - happy path")
    void saveLastPassResetDate_HappyPath() throws Exception {
        when(userService.getUserByUserName(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/v1/users/lastPassResetDate/{userName}", "testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isOk());

        verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    @DisplayName("Save last password reset date - user not found")
    void saveLastPassResetDate_UserNotFound() throws Exception {
        when(userService.getUserByUserName(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/v1/users/lastPassResetDate/{userName}", "testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserByUserName("testUser");
    }

    @Test
    @DisplayName("Update user last password reset date - happy path")
    void updateLastPasswordResetDate_HappyPath() throws Exception {
        when(userService.getUserByUserName(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/v1/users/updateLastPasswordResetDate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateLastPasswordResetFlagOnAzure(anyString());
        verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    @DisplayName("Update user last password reset date - user not found")
    void updateLastPasswordResetDate_UserNotFound() throws Exception {
        when(userService.getUserByUserName(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(put("/v1/users/updateLastPasswordResetDate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).getUserByUserName(testUser.getUserName());
    }

    @Test
    @DisplayName("Change password - happy path")
    void changePassword_HappyPath() throws Exception {
        ChangePassword changePassword = new ChangePassword();
        changePassword.setCurPassword("current");
        changePassword.setNewPassword("newPassword");
        testUser.setPassword("current");

        when(userService.getUserByUserName(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/v1/users/change-password")
                .principal(mock(Principal.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(changePassword)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    @DisplayName("Change password - current password doesn't match")
    void changePassword_CurrentPasswordMismatch() throws Exception {
        ChangePassword changePassword = new ChangePassword();
        changePassword.setCurPassword("wrongCurrent");
        changePassword.setNewPassword("newPassword");

        when(userService.getUserByUserName(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/v1/users/change-password")
                .principal(mock(Principal.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(changePassword)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).getUserByUserName(testUser.getUserName());
    }

    @Test
    @DisplayName("Change password - user not found")
    void changePassword_UserNotFound() throws Exception {
        ChangePassword changePassword = new ChangePassword();
        changePassword.setCurPassword("current");
        changePassword.setNewPassword("newPassword");

        when(userService.getUserByUserName(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(put("/v1/users/change-password")
                .principal(mock(Principal.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(changePassword)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).getUserByUserName("testUser");
    }

    @Test
    @DisplayName("Update user signature - happy path")
    void updateSignature_HappyPath() throws Exception {
        ChangeSignature changeSignature = new ChangeSignature();
        changeSignature.setSignature("New Signature");

        when(userService.getUserByUserName(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/v1/users/change-signature")
                .principal(mock(Principal.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(changeSignature)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    @DisplayName("Update user signature - user not found")
    void updateSignature_UserNotFound() throws Exception {
        ChangeSignature changeSignature = new ChangeSignature();
        changeSignature.setSignature("New Signature");

        when(userService.getUserByUserName(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(put("/v1/users/change-signature")
                .principal(mock(Principal.class))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(changeSignature)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).getUserByUserName("testUser");
    }

    @Test
    @DisplayName("Delete user - happy path")
    void deleteUser_HappyPath() throws Exception {
        mockMvc.perform(delete("/v1/users/{userId}", "1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserOnAzureAd("1");
    }
}