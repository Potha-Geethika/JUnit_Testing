package com.carbo.activitylog.services;

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
    private String jobId = "job123";
    private String orgId = "org123";

    @BeforeEach
    void setUp() {
        job = new Job();
        job.setId(jobId);
        job.setOrganizationId(orgId);
        job.setTargetStagesPerDay(5);
        job.setStartDate(System.currentTimeMillis());
    }

    @Test
    void getPadSummary_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobId(orgId, jobId)).thenReturn(Collections.emptyList());

        PadActivitySummary result = jobCompletionDashboardService.getPadSummary(request, jobId);
        
        assertNotNull(result);
    }

    @Test
    void getPadSummary_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.empty());

        PadActivitySummary result = jobCompletionDashboardService.getPadSummary(request, jobId);
        
        assertNotNull(result);
    }

    @Test
    void getStagesPerDay_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobId(orgId, jobId)).thenReturn(Collections.emptyList());

        List<StagePerDay> result = jobCompletionDashboardService.getStagesPerDay(request, jobId);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getStagesPerDay_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.empty());

        List<StagePerDay> result = jobCompletionDashboardService.getStagesPerDay(request, jobId);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getPumpHoursPerDay_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobId(orgId, jobId)).thenReturn(Collections.emptyList());

        List<PumpHoursPerDay> result = jobCompletionDashboardService.getPumpHoursPerDay(request, jobId);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getPumpHoursPerDay_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.empty());

        List<PumpHoursPerDay> result = jobCompletionDashboardService.getPumpHoursPerDay(request, jobId);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getPumpHoursPerStageFromLogs_HappyPath() {
        String wellName = "well1";
        when(jobMongoDbRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWell(orgId, jobId, wellName)).thenReturn(Collections.emptyList());

        List<PumpHoursPerStage> result = jobCompletionDashboardService.getPumpHoursPerStageFromLogs(request, jobId, wellName);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getPumpHoursPerStageFromLogs_JobNotFound() {
        String wellName = "well1";
        when(jobMongoDbRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.empty());

        List<PumpHoursPerStage> result = jobCompletionDashboardService.getPumpHoursPerStageFromLogs(request, jobId, wellName);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getServiceOrganization_HappyPath() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, orgId)).thenReturn(Optional.of(job));
        when(organizationMongoDbRepository.findById(orgId)).thenReturn(Optional.of(new Organization())); // Mock Organization

        ServiceOrganizationDetails result = jobCompletionDashboardService.getServiceOrganization(request, jobId);

        assertNotNull(result);
    }

    @Test
    void getServiceOrganization_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, orgId)).thenReturn(Optional.empty());

        ServiceOrganizationDetails result = jobCompletionDashboardService.getServiceOrganization(request, jobId);

        assertNotNull(result);
    }
}