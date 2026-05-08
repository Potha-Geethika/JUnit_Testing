package com.carbo.activitylog.controllers;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.activitylog.model.*;
import com.carbo.activitylog.services.JobCompletionDashboardService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.*;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationId;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;






@WebMvcTest(JobCompletionDashboardController.class)
public class JobCompletionDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobCompletionDashboardService jobCompletionDashboardService;

    private MockHttpServletRequest request;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-token");
    }

    @Test
    public void testIndex_HappyPath() throws Exception {
        PadActivitySummary summary = new PadActivitySummary();
        when(jobCompletionDashboardService.getPadSummary(any(), any())).thenReturn(summary);

        mockMvc.perform(get("/v1/job-complete-dashboard/activity-breakdown")
                .param("jobId", "testJobId")
                .requestAttr("request", request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.serviceOrganizationName").doesNotExist());
    }

    @Test
    public void testIndex_NotFound() throws Exception {
        when(jobCompletionDashboardService.getPadSummary(any(), any())).thenReturn(new PadActivitySummary());

        mockMvc.perform(get("/v1/job-complete-dashboard/activity-breakdown")
                .param("jobId", "nonExistentJobId")
                .requestAttr("request", request))
                .andExpect(status().isOk());
    }

    @Test
    public void testIndex_InternalServerError() throws Exception {
        when(jobCompletionDashboardService.getPadSummary(any(), any())).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/v1/job-complete-dashboard/activity-breakdown")
                .param("jobId", "testJobId")
                .requestAttr("request", request))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetStagesPerDay_HappyPath() throws Exception {
        when(jobCompletionDashboardService.getStagesPerDay(any(), any())).thenReturn(Collections.singletonList(new StagePerDay()));

        mockMvc.perform(get("/v1/job-complete-dashboard/stages-per-day")
                .param("jobId", "testJobId")
                .requestAttr("request", request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetStagesPerDay_NotFound() throws Exception {
        when(jobCompletionDashboardService.getStagesPerDay(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/job-complete-dashboard/stages-per-day")
                .param("jobId", "nonExistentJobId")
                .requestAttr("request", request))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetPumpHoursPerDay_HappyPath() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerDay(any(), any())).thenReturn(Collections.singletonList(new PumpHoursPerDay()));

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-day")
                .param("jobId", "testJobId")
                .requestAttr("request", request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetPumpHoursPerDay_NotFound() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerDay(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-day")
                .param("jobId", "nonExistentJobId")
                .requestAttr("request", request))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetPumpHoursPerStage_HappyPath() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerStageFromLogs(any(), any(), any())).thenReturn(Collections.singletonList(new PumpHoursPerStage()));

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-stage")
                .param("jobId", "testJobId")
                .param("well", "testWell")
                .requestAttr("request", request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetPumpHoursPerStage_NotFound() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerStageFromLogs(any(), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-stage")
                .param("jobId", "nonExistentJobId")
                .param("well", "testWell")
                .requestAttr("request", request))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetServiceOrganization_HappyPath() throws Exception {
        ServiceOrganizationDetails details = new ServiceOrganizationDetails();
        when(jobCompletionDashboardService.getServiceOrganization(any(), any())).thenReturn(details);

        mockMvc.perform(get("/v1/job-complete-dashboard/service-organization")
                .param("jobId", "testJobId")
                .requestAttr("request", request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetServiceOrganization_NotFound() throws Exception {
        when(jobCompletionDashboardService.getServiceOrganization(any(), any())).thenReturn(new ServiceOrganizationDetails());

        mockMvc.perform(get("/v1/job-complete-dashboard/service-organization")
                .param("jobId", "nonExistentJobId")
                .requestAttr("request", request))
                .andExpect(status().isOk());
    }
}