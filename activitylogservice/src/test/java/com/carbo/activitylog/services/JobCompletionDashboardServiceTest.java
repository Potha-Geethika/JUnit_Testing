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

    @InjectMocks
    private JobCompletionDashboardService jobCompletionDashboardService;

    @Mock
    private HttpServletRequest request;

    private Job job;
    private Organization organization;
    private ActivityLogEntry activityLogEntry;

    @BeforeEach
    void setUp() {
        job = new Job();
        job.setId("1");
        job.setOrganizationId("org1");
        job.setTargetStagesPerDay(5);
        job.setStartDate(System.currentTimeMillis());

        organization = new Organization();
        organization.setId("org1");
        organization.setName("Test Organization");

        activityLogEntry = new ActivityLogEntry();
        activityLogEntry.setOpsActivity("Pump Time");
        activityLogEntry.setJobId("1");
        activityLogEntry.setOrganizationId("org1");
    }

    @Test
    void getPadSummary_HappyPath() {
        when(request.getAttribute("organizationId")).thenReturn("org1");
        when(jobMongoDbRepository.findByIdAndOrganizationId("1", "org1")).thenReturn(Optional.of(job));
        when(organizationMongoDbRepository.findById("org1")).thenReturn(Optional.of(organization));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobId("org1", "1"))
                .thenReturn(Collections.singletonList(activityLogEntry));

        PadActivitySummary summary = jobCompletionDashboardService.getPadSummary(request, "1");

        assertNotNull(summary);
        assertEquals("Test Organization", summary.getServiceOrganizationName());
    }

    @Test
    void getStagesPerDay_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId("1", "org1")).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobId("org1", "1"))
                .thenReturn(Collections.singletonList(activityLogEntry));

        List<StagePerDay> stagesPerDay = jobCompletionDashboardService.getStagesPerDay(request, "1");

        assertNotNull(stagesPerDay);
        assertEquals(1, stagesPerDay.size());
    }

    @Test
    void getPumpHoursPerDay_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId("1", "org1")).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobId("org1", "1"))
                .thenReturn(Collections.singletonList(activityLogEntry));

        List<PumpHoursPerDay> pumpHours = jobCompletionDashboardService.getPumpHoursPerDay(request, "1");

        assertNotNull(pumpHours);
        assertEquals(1, pumpHours.size());
    }

    @Test
    void getPumpHoursPerStageFromLogs_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId("1", "org1")).thenReturn(Optional.of(job));
        when(activityLogMongoDbRepository.findByOrganizationIdAndJobId("org1", "1"))
                .thenReturn(Collections.singletonList(activityLogEntry));

        List<PumpHoursPerStage> pumpHoursPerStage = jobCompletionDashboardService.getPumpHoursPerStageFromLogs(request, "1", "Well1");

        assertNotNull(pumpHoursPerStage);
        assertEquals(1, pumpHoursPerStage.size());
    }

    @Test
    void getServiceOrganization_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId("1", "org1")).thenReturn(Optional.of(job));
        when(organizationMongoDbRepository.findById("org1")).thenReturn(Optional.of(organization));

        ServiceOrganizationDetails details = jobCompletionDashboardService.getServiceOrganization(request, "1");

        assertNotNull(details);
        assertEquals("Test Organization", details.getOrganizationName());
    }
}