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
class JobCompletionDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobCompletionDashboardService jobCompletionDashboardService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.addHeader("Organization-Id", "orgId");
    }

    @Test
    void index_HappyPath_Returns200() throws Exception {
        PadActivitySummary summary = new PadActivitySummary();
        when(jobCompletionDashboardService.getPadSummary(any(), any())).thenReturn(summary);

        mockMvc.perform(get("/v1/job-complete-dashboard/activity-breakdown")
                .param("jobId", "jobId")
                .contextPath("/v1")
                .servletPath("/activity-breakdown")
                .header("Organization-Id", "orgId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.serviceOrganizationName").doesNotExist());

        verify(jobCompletionDashboardService).getPadSummary(any(), eq("jobId"));
    }

    @Test
    void index_JobNotFound_Returns500() throws Exception {
        when(jobCompletionDashboardService.getPadSummary(any(), any())).thenReturn(null);

        mockMvc.perform(get("/v1/job-complete-dashboard/activity-breakdown")
                .param("jobId", "jobId")
                .header("Organization-Id", "orgId"))
                .andExpect(status().isInternalServerError());

        verify(jobCompletionDashboardService).getPadSummary(any(), eq("jobId"));
    }

    @Test
    void getStagesPerDay_HappyPath_Returns200() throws Exception {
        when(jobCompletionDashboardService.getStagesPerDay(any(), any())).thenReturn(Collections.singletonList(new StagePerDay()));

        mockMvc.perform(get("/v1/job-complete-dashboard/stages-per-day")
                .param("jobId", "jobId")
                .header("Organization-Id", "orgId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(jobCompletionDashboardService).getStagesPerDay(any(), eq("jobId"));
    }

    @Test
    void getStagesPerDay_JobNotFound_ReturnsEmptyList() throws Exception {
        when(jobCompletionDashboardService.getStagesPerDay(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/job-complete-dashboard/stages-per-day")
                .param("jobId", "jobId")
                .header("Organization-Id", "orgId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(jobCompletionDashboardService).getStagesPerDay(any(), eq("jobId"));
    }

    @Test
    void getPumpHoursPerDay_HappyPath_Returns200() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerDay(any(), any())).thenReturn(Collections.singletonList(new PumpHoursPerDay()));

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-day")
                .param("jobId", "jobId")
                .header("Organization-Id", "orgId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(jobCompletionDashboardService).getPumpHoursPerDay(any(), eq("jobId"));
    }

    @Test
    void getPumpHoursPerDay_JobNotFound_ReturnsEmptyList() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerDay(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-day")
                .param("jobId", "jobId")
                .header("Organization-Id", "orgId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(jobCompletionDashboardService).getPumpHoursPerDay(any(), eq("jobId"));
    }

    @Test
    void getPumpHoursPerStage_HappyPath_Returns200() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerStageFromLogs(any(), any(), any())).thenReturn(Collections.singletonList(new PumpHoursPerStage()));

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-stage")
                .param("jobId", "jobId")
                .param("well", "wellName")
                .header("Organization-Id", "orgId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(jobCompletionDashboardService).getPumpHoursPerStageFromLogs(any(), eq("jobId"), eq("wellName"));
    }

    @Test
    void getPumpHoursPerStage_JobNotFound_ReturnsEmptyList() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerStageFromLogs(any(), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-stage")
                .param("jobId", "jobId")
                .param("well", "wellName")
                .header("Organization-Id", "orgId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(jobCompletionDashboardService).getPumpHoursPerStageFromLogs(any(), eq("jobId"), eq("wellName"));
    }

    @Test
    void getServiceOrganization_HappyPath_Returns200() throws Exception {
        ServiceOrganizationDetails details = new ServiceOrganizationDetails();
        when(jobCompletionDashboardService.getServiceOrganization(any(), any())).thenReturn(details);

        mockMvc.perform(get("/v1/job-complete-dashboard/service-organization")
                .param("jobId", "jobId")
                .header("Organization-Id", "orgId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(jobCompletionDashboardService).getServiceOrganization(any(), eq("jobId"));
    }

    @Test
    void getServiceOrganization_JobNotFound_ReturnsEmptyDetails() throws Exception {
        when(jobCompletionDashboardService.getServiceOrganization(any(), any())).thenReturn(new ServiceOrganizationDetails());

        mockMvc.perform(get("/v1/job-complete-dashboard/service-organization")
                .param("jobId", "jobId")
                .header("Organization-Id", "orgId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(jobCompletionDashboardService).getServiceOrganization(any(), eq("jobId"));
    }
}