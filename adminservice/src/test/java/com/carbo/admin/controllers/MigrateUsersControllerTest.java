package com.carbo.admin.controllers;
import static org.mockito.ArgumentMatchers.any;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveAiUsersAndCollectUnsaved_HappyPath() throws Exception {
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("test@example.com");
        List<AiUser> aiUserList = Collections.singletonList(aiUser);

        when(migrateusersService.saveAiUsersAndCollectUnsaved(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/user/migrate/saveAiUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"emailAddress\":\"test@example.com\"}]"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(migrateusersService).saveAiUsersAndCollectUnsaved(any());
    }

    @Test
    void saveAiUsersAndCollectUnsaved_InternalServerError() throws Exception {
        when(migrateusersService.saveAiUsersAndCollectUnsaved(any())).thenThrow(new RuntimeException("Some error occurred"));

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/user/migrate/saveAiUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        verify(migrateusersService).saveAiUsersAndCollectUnsaved(any());
    }

    @Test
    void getAllUsersForAi_HappyPath() throws Exception {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setFirstName("First");
        userResponseDTO.setLastName("Last");
        userResponseDTO.setUserName("user1");
        userResponseDTO.setOrganizationId("org1");
        List<UserResponseDTO> responseDTOList = Collections.singletonList(userResponseDTO);

        when(migrateusersService.getAllUsersForAi()).thenReturn(responseDTOList);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/user/migrate/getAllUsersForAi"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstName").value("First"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName").value("Last"));

        verify(migrateusersService).getAllUsersForAi();
    }

    @Test
    void setUserAzureId_HappyPath() throws Exception {
        AiUser aiUser = new AiUser();
        aiUser.setEmailAddress("test@example.com");
        List<AiUser> aiUserList = Collections.singletonList(aiUser);
        User user = new User();
        user.setAzureId("azure-id");

        when(migrateusersService.setUserAzureId(any())).thenReturn(Collections.singletonList(user));

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/user/migrate/setUserAzureId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"emailAddress\":\"test@example.com\"}]"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].azureId").value("azure-id"));

        verify(migrateusersService).setUserAzureId(any());
    }

    @Test
    void setUserAzureId_InternalServerError() throws Exception {
        when(migrateusersService.setUserAzureId(any())).thenThrow(new RuntimeException("Some error occurred"));

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/user/migrate/setUserAzureId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        verify(migrateusersService).setUserAzureId(any());
    }
}