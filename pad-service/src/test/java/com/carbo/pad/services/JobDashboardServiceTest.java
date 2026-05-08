package com.carbo.pad.services;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        job = new Job();
        job.setOrganizationId("orgId");
        job.setId("jobId");
        job.setPad("padName");
        job.setWells(Collections.emptyList());
    }

    @Test
    void getPadDetails_HappyPath() {
        when(request.getHeader("Time-Zone")).thenReturn("UTC");
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(mongoTemplate.find(any(), eq(ActivityLogEntry.class), any())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = jobDashboardService.getPadDetails(request, "jobId");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getPadDetails_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getPadDetails(request, "jobId");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getWellCompletionInformation_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(mongoTemplate.find(any(), eq(Well.class), any())).thenReturn(Collections.singletonList(new Well()));

        ResponseEntity<?> response = jobDashboardService.getWellCompletionInformation(request, "jobId", "wellId");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getWellCompletionInformation_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getWellCompletionInformation(request, "jobId", "wellId");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCleanPerStage_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(mongoTemplate.find(any(), eq(ChemicalStage.class), any())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = jobDashboardService.getCleanPerStage(request, "jobId", "wellId");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getCleanPerStage_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getCleanPerStage(request, "jobId", "wellId");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAveragePressureAndRate_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(mongoTemplate.find(any(), eq(Well.class), any())).thenReturn(Collections.singletonList(new Well()));

        ResponseEntity<?> response = jobDashboardService.getAveragePressureAndRate(request, "jobId", "wellId");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAveragePressureAndRate_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getAveragePressureAndRate(request, "jobId", "wellId");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getFinalISIPAndFG_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(mongoTemplate.find(any(), eq(Well.class), any())).thenReturn(Collections.singletonList(new Well()));

        ResponseEntity<?> response = jobDashboardService.getFinalISIPAndFG(request, "jobId", "wellId");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getFinalISIPAndFG_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getFinalISIPAndFG(request, "jobId", "wellId");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAverageVsMax_HappyPath() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.of(job));
        when(mongoTemplate.find(any(), eq(ChemicalStage.class), any())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = jobDashboardService.getAverageVsMax(request, "jobId", "wellId");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAverageVsMax_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndOrganizationId(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getAverageVsMax(request, "jobId", "wellId");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}