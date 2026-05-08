package com.carbo.activitylog.services;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.activitylog.model.*;
import com.carbo.activitylog.model.Job;
import com.carbo.activitylog.repository.ActivityLogMongoDbRepository;
import com.carbo.activitylog.repository.JobMongoDbRepository;
import com.carbo.activitylog.repository.OrganizationMongoDbRepository;
import com.carbo.activitylog.utils.Constants;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import static com.carbo.activitylog.utils.CommonUtils.resolveTimeZone;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationId;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationName;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;






@ExtendWith(MockitoExtension.class)
class JobCompletionDashboardServiceTest {

    @Mock
    private ActivityLogMongoDbRepository activityLogMongoDbRepository;

    @Mock
    private JobMongoDbRepository jobMongoDbRepository;

    @Mock
    private OrganizationMongoDbRepository organizationMongoDbRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JobCompletionDashboardService jobCompletionDashboardService;

    private Job job;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        job = new Job();
        job.setId("jobId");
        job.setOrganizationId("orgId");
        job.setTargetStagesPerDay(5);
        job.setStartDate(System.currentTimeMillis());
    }

    @Test
    void getPadSummary_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobId(any(), any())).thenReturn(Collections.emptyList());

        PadActivitySummary result = jobCompletionDashboardService.getPadSummary(request, "jobId");

        assertNotNull(result);
        assertEquals(0, result.getPadActivityBreakdown().getTotalActivityhours());
    }

    @Test
    void getPadSummary_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        PadActivitySummary result = jobCompletionDashboardService.getPadSummary(request, "jobId");

        assertNotNull(result);
        assertEquals(0, result.getPadActivityBreakdown().getTotalActivityhours());
    }

    @Test
    void getStagesPerDay_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobId(any(), any())).thenReturn(Collections.emptyList());

        List<StagePerDay> result = jobCompletionDashboardService.getStagesPerDay(request, "jobId");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getStagesPerDay_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        List<StagePerDay> result = jobCompletionDashboardService.getStagesPerDay(request, "jobId");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPumpHoursPerDay_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobId(any(), any())).thenReturn(Collections.emptyList());

        List<PumpHoursPerDay> result = jobCompletionDashboardService.getPumpHoursPerDay(request, "jobId");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPumpHoursPerDay_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        List<PumpHoursPerDay> result = jobCompletionDashboardService.getPumpHoursPerDay(request, "jobId");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPumpHoursPerStageFromLogs_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWell(any(), any(), any())).thenReturn(Collections.emptyList());

        List<PumpHoursPerStage> result = jobCompletionDashboardService.getPumpHoursPerStageFromLogs(request, "jobId", "wellName");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPumpHoursPerStageFromLogs_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        List<PumpHoursPerStage> result = jobCompletionDashboardService.getPumpHoursPerStageFromLogs(request, "jobId", "wellName");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getServiceOrganization_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(organizationMongoDbRepository.findById(any())).thenReturn(Optional.of(new Organization()));

        ServiceOrganizationDetails result = jobCompletionDashboardService.getServiceOrganization(request, "jobId");

        assertNotNull(result);
        assertEquals("orgId", result.getOrganizationId());
    }

    @Test
    void getServiceOrganization_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ServiceOrganizationDetails result = jobCompletionDashboardService.getServiceOrganization(request, "jobId");

        assertNotNull(result);
        assertEquals(null, result.getOrganizationId());
    }
}