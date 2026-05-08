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

    @Mock private MongoTemplate mongoTemplate;
    @Mock private JobMongoDbRepository jobMongoDbRepository;
    @Mock private HttpServletRequest request;
    
    @InjectMocks private JobDashboardService jobDashboardService;

    private Job job;
    private String jobId = "jobId";
    private String wellId = "wellId";

    @BeforeEach
    void setUp() {
        job = new Job();
        job.setId(jobId);
        job.setOrganizationId("orgId");
        job.setPad("padName");
    }

    @Test
    void getPadDetails_HappyPath() {
        when(request.getHeader("Time-Zone")).thenReturn("UTC");
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "orgId")).thenReturn(Optional.of(job));
        when(mongoTemplate.find(any(), any(), any())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = jobDashboardService.getPadDetails(request, jobId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getPadDetails_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "orgId")).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getPadDetails(request, jobId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getWellCompletionInformation_HappyPath() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "orgId")).thenReturn(Optional.of(job));
        when(mongoTemplate.findOne(any(), any(), any())).thenReturn(new Well());

        ResponseEntity<?> response = jobDashboardService.getWellCompletionInformation(request, jobId, wellId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getWellCompletionInformation_JobNotFound() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "orgId")).thenReturn(Optional.empty());

        ResponseEntity<?> response = jobDashboardService.getWellCompletionInformation(request, jobId, wellId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCleanPerStage_HappyPath() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "orgId")).thenReturn(Optional.of(job));
        when(mongoTemplate.find(any(), any(), any())).thenReturn(Collections.singletonList(new ChemicalStage()));

        ResponseEntity<?> response = jobDashboardService.getCleanPerStage(request, jobId, wellId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getAveragePressureAndRate_HappyPath() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "orgId")).thenReturn(Optional.of(job));
        when(mongoTemplate.findOne(any(), any(), any())).thenReturn(new Well());
        when(mongoTemplate.find(any(), any(), any())).thenReturn(Collections.singletonList(new EndStageEmailPayload()));

        ResponseEntity<?> response = jobDashboardService.getAveragePressureAndRate(request, jobId, wellId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getAverageVsMax_HappyPath() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "orgId")).thenReturn(Optional.of(job));
        when(mongoTemplate.findOne(any(), any(), any())).thenReturn(new Well());
        when(mongoTemplate.find(any(), any(), any())).thenReturn(Collections.singletonList(new ChemicalStage()));

        ResponseEntity<?> response = jobDashboardService.getAverageVsMax(request, jobId, wellId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getFinalISIPAndFG_HappyPath() {
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "orgId")).thenReturn(Optional.of(job));
        when(mongoTemplate.findOne(any(), any(), any())).thenReturn(new Well());
        when(mongoTemplate.find(any(), any(), any())).thenReturn(Collections.singletonList(new ChemicalStage()));

        ResponseEntity<?> response = jobDashboardService.getFinalISIPAndFG(request, jobId, wellId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getPadDurationDays_HappyPath() {
        double duration = jobDashboardService.calculatePadDurationDays(job, Collections.emptyList(), "UTC");
        assertEquals(0.01, duration);
    }
}