package com.carbo.admin.controllers;

import com.carbo.admin.model.User;
import com.carbo.admin.model.azureB2C.AiUser;
import com.carbo.admin.model.azureB2C.UserResponseDTO;
import com.carbo.admin.services.MigrateUsersService;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;






@WebMvcTest(MigrateUsersController.class)
class MigrateUsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MigrateUsersService migrateusersService;

    private AiUser aiUser;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        aiUser = new AiUser("azureId", 1, "tenantName", 1, "userName", "name", "surname", "email@example.com", "mobileNumber", "role", "notificationType", "status");
        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setFirstName("First");
        userResponseDTO.setLastName("Last");
        userResponseDTO.setUserName("userName");
        userResponseDTO.setOrganizationId("orgId");
        userResponseDTO.setOrganizationName("orgName");
        userResponseDTO.setAzureId("azureId");
    }

    @Test
    void saveAiUsersAndCollectUnsaved_happyPath() throws Exception {
        when(migrateusersService.saveAiUsersAndCollectUnsaved(anyList())).thenReturn(Collections.singletonList(aiUser));

        mockMvc.perform(post("/v1/user/migrate/saveAiUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"azureUserId\":\"azureId\",\"tenantId\":1,\"tenantName\":\"tenantName\",\"id\":1,\"userName\":\"userName\",\"name\":\"name\",\"surname\":\"surname\",\"emailAddress\":\"email@example.com\",\"mobileNumber\":\"mobileNumber\",\"role\":\"role\",\"notificationType\":\"notificationType\",\"status\":\"status\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].azureUserId").value("azureId"))
                .andExpect(jsonPath("$[0].tenantId").value(1));
    }

    @Test
    void saveAiUsersAndCollectUnsaved_internalServerError() throws Exception {
        when(migrateusersService.saveAiUsersAndCollectUnsaved(anyList())).thenThrow(new RuntimeException("Some error occurred"));

        mockMvc.perform(post("/v1/user/migrate/saveAiUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"azureUserId\":\"azureId\",\"tenantId\":1,\"tenantName\":\"tenantName\",\"id\":1,\"userName\":\"userName\",\"name\":\"name\",\"surname\":\"surname\",\"emailAddress\":\"email@example.com\",\"mobileNumber\":\"mobileNumber\",\"role\":\"role\",\"notificationType\":\"notificationType\",\"status\":\"status\"}]"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllUsersForAi_happyPath() throws Exception {
        when(migrateusersService.getAllUsersForAi()).thenReturn(Collections.singletonList(userResponseDTO));

        mockMvc.perform(get("/v1/user/migrate/getAllUsersForAi")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("First"))
                .andExpect(jsonPath("$[0].lastName").value("Last"));
    }

    @Test
    void setUserAzureId_happyPath() throws Exception {
        when(migrateusersService.setUserAzureId(anyList())).thenReturn(Collections.singletonList(new User()));

        mockMvc.perform(put("/v1/user/migrate/setUserAzureId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"azureUserId\":\"azureId\",\"tenantId\":1,\"tenantName\":\"tenantName\",\"id\":1,\"userName\":\"userName\",\"name\":\"name\",\"surname\":\"surname\",\"emailAddress\":\"email@example.com\",\"mobileNumber\":\"mobileNumber\",\"role\":\"role\",\"notificationType\":\"notificationType\",\"status\":\"status\"}]"))
                .andExpect(status().isOk());
    }

    @Test
    void setUserAzureId_internalServerError() throws Exception {
        when(migrateusersService.setUserAzureId(anyList())).thenThrow(new RuntimeException("Some error occurred"));

        mockMvc.perform(put("/v1/user/migrate/setUserAzureId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"azureUserId\":\"azureId\",\"tenantId\":1,\"tenantName\":\"tenantName\",\"id\":1,\"userName\":\"userName\",\"name\":\"name\",\"surname\":\"surname\",\"emailAddress\":\"email@example.com\",\"mobileNumber\":\"mobileNumber\",\"role\":\"role\",\"notificationType\":\"notificationType\",\"status\":\"status\"}]"))
                .andExpect(status().isInternalServerError());
    }
}