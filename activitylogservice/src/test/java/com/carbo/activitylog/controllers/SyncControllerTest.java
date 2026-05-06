package com.carbo.activitylog.controllers;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.activitylog.model.ActivityLogEntry;
import com.carbo.activitylog.model.DeletedActivityLogEntry;
import com.carbo.activitylog.model.SyncRequest;
import com.carbo.activitylog.model.SyncResponse;
import com.carbo.activitylog.services.ActivityLogService;
import com.carbo.activitylog.services.DeletedActivityLogService;
import com.carbo.activitylog.utils.Constants;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
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
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationId;
import static com.carbo.activitylog.utils.ControllerUtil.getUserFullName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;







@WebMvcTest(SyncController.class)
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityLogService activityLogService;

    @MockBean
    private DeletedActivityLogService deletedActivityLogService;

    @MockBean
    private ModelMapper modelMapper;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    void view_HappyPath_ShouldReturn200() throws Exception {
        String jobId = "testJobId";
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setId("entryId");
        entry.setTs(123456789L);

        when(activityLogService.findByOrganizationIdAndJobId(any(String.class), any(String.class)))
                .thenReturn(Collections.singletonList(entry));

        mockMvc.perform(get("/v1/sync/view?jobId=" + jobId)
                .requestAttr("organizationId", "orgId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryId").value("entryId"))
                .andExpect(jsonPath("$.ts").value(123456789L));

        verify(activityLogService).findByOrganizationIdAndJobId(any(String.class), eq(jobId));
    }



}