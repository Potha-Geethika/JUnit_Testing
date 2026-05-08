package com.carbo.activitylog.controllers;
import static org.mockito.ArgumentMatchers.anyString;
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

    @MockBean
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        reset(jobCompletionDashboardService);
    }

    @Test
    void index_HappyPath_Returns200() throws Exception {
        PadActivitySummary summary = new PadActivitySummary();
        when(jobCompletionDashboardService.getPadSummary(any(HttpServletRequest.class), anyString())).thenReturn(summary);

        mockMvc.perform(get("/v1/job-complete-dashboard/activity-breakdown")
                .param("jobId", "12345")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceOrganizationName").exists()); // Assuming the structure has this field

        verify(jobCompletionDashboardService).getPadSummary(any(HttpServletRequest.class), anyString());
    }

    @Test
    void index_JobNotFound_Returns500() throws Exception {
        when(jobCompletionDashboardService.getPadSummary(any(HttpServletRequest.class), anyString())).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/v1/job-complete-dashboard/activity-breakdown")
                .param("jobId", "unknownJobId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(jobCompletionDashboardService).getPadSummary(any(HttpServletRequest.class), anyString());
    }

    @Test
    void getStagesPerDay_HappyPath_Returns200() throws Exception {
        when(jobCompletionDashboardService.getStagesPerDay(any(HttpServletRequest.class), anyString())).thenReturn(Collections.singletonList(new StagePerDay()));

        mockMvc.perform(get("/v1/job-complete-dashboard/stages-per-day")
                .param("jobId", "12345")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(jobCompletionDashboardService).getStagesPerDay(any(HttpServletRequest.class), anyString());
    }

    @Test
    void getStagesPerDay_JobNotFound_Returns500() throws Exception {
        when(jobCompletionDashboardService.getStagesPerDay(any(HttpServletRequest.class), anyString())).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/v1/job-complete-dashboard/stages-per-day")
                .param("jobId", "unknownJobId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(jobCompletionDashboardService).getStagesPerDay(any(HttpServletRequest.class), anyString());
    }

    @Test
    void getPumpHoursPerDay_HappyPath_Returns200() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerDay(any(HttpServletRequest.class), anyString())).thenReturn(Collections.singletonList(new PumpHoursPerDay()));

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-day")
                .param("jobId", "12345")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(jobCompletionDashboardService).getPumpHoursPerDay(any(HttpServletRequest.class), anyString());
    }

    @Test
    void getPumpHoursPerDay_JobNotFound_Returns500() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerDay(any(HttpServletRequest.class), anyString())).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-day")
                .param("jobId", "unknownJobId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(jobCompletionDashboardService).getPumpHoursPerDay(any(HttpServletRequest.class), anyString());
    }

    @Test
    void getPumpHoursPerStage_HappyPath_Returns200() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerStageFromLogs(any(HttpServletRequest.class), anyString(), anyString()))
                .thenReturn(Collections.singletonList(new PumpHoursPerStage()));

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-stage")
                .param("jobId", "12345")
                .param("well", "wellName")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(jobCompletionDashboardService).getPumpHoursPerStageFromLogs(any(HttpServletRequest.class), anyString(), anyString());
    }

    @Test
    void getPumpHoursPerStage_JobNotFound_Returns500() throws Exception {
        when(jobCompletionDashboardService.getPumpHoursPerStageFromLogs(any(HttpServletRequest.class), anyString(), anyString()))
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-stage")
                .param("jobId", "unknownJobId")
                .param("well", "wellName")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(jobCompletionDashboardService).getPumpHoursPerStageFromLogs(any(HttpServletRequest.class), anyString(), anyString());
    }

    @Test
    void getServiceOrganization_HappyPath_Returns200() throws Exception {
        ServiceOrganizationDetails details = new ServiceOrganizationDetails();
        when(jobCompletionDashboardService.getServiceOrganization(any(HttpServletRequest.class), anyString())).thenReturn(details);

        mockMvc.perform(get("/v1/job-complete-dashboard/service-organization")
                .param("jobId", "12345")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").exists()); // Assuming the structure has this field

        verify(jobCompletionDashboardService).getServiceOrganization(any(HttpServletRequest.class), anyString());
    }

    @Test
    void getServiceOrganization_JobNotFound_Returns500() throws Exception {
        when(jobCompletionDashboardService.getServiceOrganization(any(HttpServletRequest.class), anyString())).thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/v1/job-complete-dashboard/service-organization")
                .param("jobId", "unknownJobId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(jobCompletionDashboardService).getServiceOrganization(any(HttpServletRequest.class), anyString());
    }
}