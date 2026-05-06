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
public class MigrateUsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MigrateUsersService migrateusersService;

    private List<AiUser> aiUserList;
    private List<UserResponseDTO> userResponseDTOList;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        aiUserList = Arrays.asList(
            new AiUser("azureId1", 1, "Tenant1", 1, "User1", "Name1", "Surname1", "email1@example.com"),
            new AiUser("azureId2", 2, "Tenant2", 2, "User2", "Name2", "Surname2", "email2@example.com")

        userResponseDTOList = Collections.singletonList(new UserResponseDTO("Name1", "Surname1", "email1@example.com", "orgId1", "Organization1", "azureId1"));
        userList = Collections.singletonList(new User());
    }

    @Test
    void testSaveAiUsersAndCollectUnsaved() throws Exception {
        when(migrateusersService.saveAiUsersAndCollectUnsaved(anyList())).thenReturn(Arrays.asList(aiUserList.get(0)));

        mockMvc.perform(post("/v1/user/migrate/saveAiUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"azureUserId\":\"azureId1\",\"tenantId\":1,\"tenantName\":\"Tenant1\",\"id\":1,\"userName\":\"User1\",\"name\":\"Name1\",\"surname\":\"Surname1\",\"emailAddress\":\"email1@example.com\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].azureUserId").value("azureId1"));

        verify(migrateusersService, times(1)).saveAiUsersAndCollectUnsaved(anyList());
    }

    @Test
    void testGetAllUsersForAi() throws Exception {
        when(migrateusersService.getAllUsersForAi()).thenReturn(userResponseDTOList);

        mockMvc.perform(get("/v1/user/migrate/getAllUsersForAi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Name1"));

        verify(migrateusersService, times(1)).getAllUsersForAi();
    }

    @Test
    void testSetUserAzureId() throws Exception {
        when(migrateusersService.setUserAzureId(anyList())).thenReturn(userList);

        mockMvc.perform(put("/v1/user/migrate/setUserAzureId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"azureUserId\":\"azureId1\",\"tenantId\":1,\"tenantName\":\"Tenant1\",\"id\":1,\"userName\":\"User1\",\"name\":\"Name1\",\"surname\":\"Surname1\",\"emailAddress\":\"email1@example.com\"}]"))
                .andExpect(status().isOk());

        verify(migrateusersService, times(1)).setUserAzureId(anyList());
    }

    @Test
    void testSaveAiUsersAndCollectUnsaved_InternalServerError() throws Exception {
        when(migrateusersService.saveAiUsersAndCollectUnsaved(anyList())).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(post("/v1/user/migrate/saveAiUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"azureUserId\":\"azureId1\",\"tenantId\":1,\"tenantName\":\"Tenant1\",\"id\":1,\"userName\":\"User1\",\"name\":\"Name1\",\"surname\":\"Surname1\",\"emailAddress\":\"email1@example.com\"}]"))
                .andExpect(status().isInternalServerError());

        verify(migrateusersService, times(1)).saveAiUsersAndCollectUnsaved(anyList());
    }

    @Test
    void testSetUserAzureId_InternalServerError() throws Exception {
        when(migrateusersService.setUserAzureId(anyList())).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(put("/v1/user/migrate/setUserAzureId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"azureUserId\":\"azureId1\",\"tenantId\":1,\"tenantName\":\"Tenant1\",\"id\":1,\"userName\":\"User1\",\"name\":\"Name1\",\"surname\":\"Surname1\",\"emailAddress\":\"email1@example.com\"}]"))
                .andExpect(status().isInternalServerError());

        verify(migrateusersService, times(1)).setUserAzureId(anyList());
    }
}