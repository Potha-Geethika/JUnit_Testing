package com.carbo.activitylog.controllers;

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

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    void index_ShouldReturnPadActivitySummary_WhenJobExists() throws Exception {
        String jobId = "jobId1";
        PadActivitySummary summary = new PadActivitySummary();
        // Initialize summary with required fields

        when(jobCompletionDashboardService.getPadSummary(request, jobId)).thenReturn(summary);

        mockMvc.perform(get("/v1/job-complete-dashboard/activity-breakdown")
                .param("jobId", jobId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceOrganizationName").exists()); // Assuming this field exists

        verify(jobCompletionDashboardService).getPadSummary(request, jobId);
    }

    @Test
    void index_ShouldReturnEmptyPadActivitySummary_WhenJobDoesNotExist() throws Exception {
        String jobId = "jobId1";
        when(jobCompletionDashboardService.getPadSummary(request, jobId)).thenReturn(new PadActivitySummary());

        mockMvc.perform(get("/v1/job-complete-dashboard/activity-breakdown")
                .param("jobId", jobId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceOrganizationName").doesNotExist());

        verify(jobCompletionDashboardService).getPadSummary(request, jobId);
    }

    @Test
    void getStagesPerDay_ShouldReturnStagesList_WhenJobExists() throws Exception {
        String jobId = "jobId1";
        List<StagePerDay> stages = Collections.singletonList(new StagePerDay());
        when(jobCompletionDashboardService.getStagesPerDay(request, jobId)).thenReturn(stages);

        mockMvc.perform(get("/v1/job-complete-dashboard/stages-per-day")
                .param("jobId", jobId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());

        verify(jobCompletionDashboardService).getStagesPerDay(request, jobId);
    }

    @Test
    void getPumpHoursPerDay_ShouldReturnPumpHoursList_WhenJobExists() throws Exception {
        String jobId = "jobId1";
        List<PumpHoursPerDay> pumpHours = Collections.singletonList(new PumpHoursPerDay());
        when(jobCompletionDashboardService.getPumpHoursPerDay(request, jobId)).thenReturn(pumpHours);

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-day")
                .param("jobId", jobId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());

        verify(jobCompletionDashboardService).getPumpHoursPerDay(request, jobId);
    }

    @Test
    void getPumpHoursPerStage_ShouldReturnPumpHoursPerStage_WhenJobExists() throws Exception {
        String jobId = "jobId1";
        String wellName = "well1";
        List<PumpHoursPerStage> pumpHoursPerStage = Collections.singletonList(new PumpHoursPerStage());
        when(jobCompletionDashboardService.getPumpHoursPerStageFromLogs(request, jobId, wellName)).thenReturn(pumpHoursPerStage);

        mockMvc.perform(get("/v1/job-complete-dashboard/pump-hours-per-stage")
                .param("jobId", jobId)
                .param("well", wellName)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());

        verify(jobCompletionDashboardService).getPumpHoursPerStageFromLogs(request, jobId, wellName);
    }

    @Test
    void getServiceOrganization_ShouldReturnServiceOrganizationDetails_WhenJobExists() throws Exception {
        String jobId = "jobId1";
        ServiceOrganizationDetails details = new ServiceOrganizationDetails();
        when(jobCompletionDashboardService.getServiceOrganization(request, jobId)).thenReturn(details);

        mockMvc.perform(get("/v1/job-complete-dashboard/service-organization")
                .param("jobId", jobId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationId").exists()); // Assuming this field exists

        verify(jobCompletionDashboardService).getServiceOrganization(request, jobId);
    }
}