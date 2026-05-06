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






public class UserServiceControllerTest {

    @InjectMocks
    private UserServiceController userServiceController;

    @Mock
    private UserService userService;

    @Mock
    private DistrictService districtService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userServiceController).build();
    }

    @Test
    public void getUsers_HappyPath() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(new User());
        when(userService.getAll()).thenReturn(users);

        mockMvc.perform(get("/v1/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        verify(userService, times(1)).getAll();
    }

    @Test
    public void getUser_HappyPath() throws Exception {
        User user = new User();
        when(userService.getUser("userId")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/v1/users/{userId}", "userId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()));

        verify(userService, times(1)).getUser("userId");
    }

    @Test
    public void getUser_UserNotFound() throws Exception {
        when(userService.getUser("userId")).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/users/{userId}", "userId"))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getUser("userId");
    }

    @Test
    public void saveUser_HappyPath() throws Exception {
        User user = new User();
        user.setUserName("test@example.com");
        String newPassword = "generatedPassword";

        when(userService.saveUserOnAzureAd(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/v1/users/")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isOk());

        verify(userService, times(1)).saveUserOnAzureAd(any(User.class));
    }

    @Test
    public void saveUser_DuplicateKeyException() throws Exception {
        User user = new User();
        when(userService.saveUserOnAzureAd(any(User.class))).thenThrow(new DuplicateKeyException("User already exists"));

        mockMvc.perform(post("/v1/users/")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).saveUserOnAzureAd(any(User.class));
    }

    @Test
    public void saveUser_MongoWriteException() throws Exception {
        User user = new User();
        when(userService.saveUserOnAzureAd(any(User.class))).thenThrow(new MongoWriteException(null, null));

        mockMvc.perform(post("/v1/users/")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).saveUserOnAzureAd(any(User.class));
    }

    @Test
    public void updateUser_HappyPath() throws Exception {
        User user = new User();
        user.setUserName("test@example.com");
        when(userService.getUser("userId")).thenReturn(Optional.of(user));

        ResponseEntity responseEntity = ResponseEntity.ok().build();
        when(userService.updateUserOnAzureAd(any(User.class), anyString())).thenReturn(responseEntity);

        mockMvc.perform(put("/v1/users/{userId}", "userId")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUserOnAzureAd(any(User.class), anyString());
    }

    @Test
    public void updateUser_UserNotFound() throws Exception {
        User user = new User();
        when(userService.getUser("userId")).thenReturn(Optional.empty());

        mockMvc.perform(put("/v1/users/{userId}", "userId")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getUser("userId");
    }

    @Test
    public void deleteUser_HappyPath() throws Exception {
        doNothing().when(userService).deleteUserOnAzureAd("userId");

        mockMvc.perform(delete("/v1/users/{userId}", "userId"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserOnAzureAd("userId");
    }
}