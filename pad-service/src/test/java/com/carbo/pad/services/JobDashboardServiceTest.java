package com.carbo.pad.services;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.pad.model.*;
import com.carbo.pad.model.Error;
import com.carbo.pad.repository.JobMongoDbRepository;
import com.carbo.pad.utils.ActivityLogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.*;
import java.util.stream.Collectors;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;
import static com.carbo.pad.utils.ActivityLogUtil.round;
import static com.carbo.pad.utils.ControllerUtil.getOrganization;
import static com.carbo.pad.utils.ControllerUtil.getOrganizationId;
import static com.carbo.pad.utils.ControllerUtil.getOrganizationType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;






@ExtendWith(MockitoExtension.class)
class JobDashboardServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private AICallsService aiCallsService;

    @Mock
    private JobMongoDbRepository jobMongoDbRepository;

    @InjectMocks
    private JobDashboardService jobDashboardService;

    @Mock
    private HttpServletRequest request;

    private Job job;
    private String jobId = "jobId";
    private String organizationId = "orgId";
    private String wellId = "wellId";

    @BeforeEach
    void setUp() {
        job = new Job();
        job.setId(jobId);
        job.setOrganizationId(organizationId);
        job.setPad("Pad1");
        job.setStartDate(System.currentTimeMillis());
        job.setStatus("COMPLETED");
    }

    @Test
    void getPadDetails_HappyPath() {
        when(request.getHeader("Time-Zone")).thenReturn("UTC");
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));

        List<ActivityLogEntry> activityLogEntries = Collections.singletonList(new ActivityLogEntry());
        when(mongoTemplate.find(any(), eq(ActivityLogEntry.class), any())).thenReturn(activityLogEntries);

        PadDetailsResponse expectedResponse = new PadDetailsResponse();
        expectedResponse.setWells(Collections.emptyList());
        expectedResponse.setDailyAverages(new DailyAverages());
        expectedResponse.setPadTotals(new PadTotals());
        expectedResponse.setCalculatedOverDays(0.01);

        ResponseEntity<?> response = jobDashboardService.getPadDetails(request, jobId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getPadDetails_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenReturn(Optional.empty());
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getPadDetails(request, jobId);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getPadDetails_ExceptionHandling() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = jobDashboardService.getPadDetails(request, jobId);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getWellCompletionInformation_HappyPath() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));

        WellCompletionInformation expectedInfo = new WellCompletionInformation();
        when(aiCallsService.getFracProTreatmentsListForCurWellDirect(anyInt(), anyString(), any())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = jobDashboardService.getWellCompletionInformation(request, jobId, wellId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getWellCompletionInformation_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenReturn(Optional.empty());
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getWellCompletionInformation(request, jobId, wellId);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getWellCompletionInformation_ExceptionHandling() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = jobDashboardService.getWellCompletionInformation(request, jobId, wellId);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getCleanPerStage_HappyPath() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));

        List<ChemicalStage> stages = Collections.singletonList(new ChemicalStage());
        when(mongoTemplate.find(any(), eq(ChemicalStage.class), any())).thenReturn(stages);

        ResponseEntity<?> response = jobDashboardService.getCleanPerStage(request, jobId, wellId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getCleanPerStage_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenReturn(Optional.empty());
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getCleanPerStage(request, jobId, wellId);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCleanPerStage_ExceptionHandling() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = jobDashboardService.getCleanPerStage(request, jobId, wellId);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getAveragePressureAndRate_HappyPath() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));

        List<EndStageEmailPayload> endStageEmails = Collections.singletonList(new EndStageEmailPayload());
        when(mongoTemplate.find(any(), eq(EndStageEmailPayload.class), any())).thenReturn(endStageEmails);

        ResponseEntity<?> response = jobDashboardService.getAveragePressureAndRate(request, jobId, wellId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAveragePressureAndRate_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenReturn(Optional.empty());
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getAveragePressureAndRate(request, jobId, wellId);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAveragePressureAndRate_ExceptionHandling() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(any(), any())).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = jobDashboardService.getAveragePressureAndRate(request, jobId, wellId);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Further tests for other methods would follow a similar pattern...
}