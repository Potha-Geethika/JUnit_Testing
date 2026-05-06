package com.carbo.activitylog.controllers;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.activitylog.model.*;
import com.carbo.activitylog.services.*;
import com.carbo.activitylog.utils.Constants;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.io.IOException;
import java.math.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
import java.time.ZoneId;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import static com.carbo.activitylog.utils.Constants.OPERATOR;
import static com.carbo.activitylog.utils.ControllerUtil.getCurDay;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationId;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;






@WebMvcTest(ActivityLogServiceController.class)
class ActivityLogServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityLogService activityLogService;

    @MockBean
    private DistrictFleetTimeZoneService districtFleetTimeZoneService;

    @MockBean
    private JobService jobService;

    @MockBean
    private OperatorService operatorService;

    @MockBean
    private OrganizationService organizationService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        // Set up necessary headers or attributes if needed
    }

    @Test
    void getActivityLogs_HappyPath() throws Exception {
        ActivityLogEntry logEntry = new ActivityLogEntry();
        when(activityLogService.findByOrganizationIdAndJobIdAndDay(any(), any(), any()))
                .thenReturn(Collections.singletonList(logEntry));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/activity-logs/")
                        .param("jobId", "testJobId")
                        .param("day", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists());

        verify(activityLogService).findByOrganizationIdAndJobIdAndDay(any(), eq("testJobId"), eq(1));
    }

    @Test
    void getActivityLogs_IllegalArgumentException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/activity-logs/"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verifyNoInteractions(activityLogService);
    }

    @Test
    void getActivityByOrganizationIdAndJobId_HappyPath() throws Exception {
        ActivityLogEntry logEntry = new ActivityLogEntry();
        when(activityLogService.findByOrganizationIdAndJobId(any(), any()))
                .thenReturn(Collections.singletonList(logEntry));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/activity-logs/getActivityByOrganizationIdAndJobId")
                        .param("jobId", "testJobId"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists());

        verify(activityLogService).findByOrganizationIdAndJobId(any(), eq("testJobId"));
    }

    @Test
    void getActivityByOrganizationIdAndJobId_IllegalArgumentException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/activity-logs/getActivityByOrganizationIdAndJobId"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verifyNoInteractions(activityLogService);
    }

    @Test
    void getActivityLogSummary_HappyPath() throws Exception {
        ActivityLogSummary summary = new ActivityLogSummary();
        Job job = new Job();
        job.setStartDate(null); // Set as needed for the test
        when(jobService.getByOrganizationIdAndJobId(any(), any())).thenReturn(Optional.of(job));
        when(districtFleetTimeZoneService.getZone(any(), any())).thenReturn(ZoneId.of("UTC"));
        when(activityLogService.findByOrganizationIdAndJobId(any(), any())).thenReturn(Collections.emptyList());
        when(organizationService.get(any())).thenReturn(Optional.of(new Organization()));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/activity-logs/summary")
                        .param("jobId", "testJobId"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

        verify(activityLogService).findByOrganizationIdAndJobId(any(), eq("testJobId"));
    }

    @Test
    void getActivityLogSummary_AccessDeniedException() throws Exception {
        when(jobService.getByOrganizationIdAndJobId(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/activity-logs/summary")
                        .param("jobId", "testJobId"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        verifyNoInteractions(activityLogService);
    }

    @Test
    void getActivityLog_HappyPath() throws Exception {
        ActivityLogEntry logEntry = new ActivityLogEntry();
        when(activityLogService.getActivityLog(any())).thenReturn(Optional.of(logEntry));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/activity-logs/{activityLogId}", "logId"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists());

        verify(activityLogService).getActivityLog(eq("logId"));
    }

    @Test
    void getActivityLog_NotFound() throws Exception {
        when(activityLogService.getActivityLog(any())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/activity-logs/{activityLogId}", "logId"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        verify(activityLogService).getActivityLog(eq("logId"));
    }

    // Additional tests for other methods...

}