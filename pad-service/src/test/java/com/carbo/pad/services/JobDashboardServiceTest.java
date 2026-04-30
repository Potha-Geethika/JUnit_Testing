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






class JobDashboardServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private AICallsService aiCallsService;

    @Mock
    private JobMongoDbRepository jobMongoDbRepository;

    @InjectMocks
    private JobDashboardService jobDashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPadDetails_HappyPath() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Job job = new Job();
        job.setOrganizationId("org123");
        String jobId = "job123";
        when(request.getHeader("Time-Zone")).thenReturn("UTC");
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.of(job));
        when(mongoTemplate.find(any(), eq(ActivityLogEntry.class), anyString()))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<?> response = jobDashboardService.getPadDetails(request, jobId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetPadDetails_JobNotFound() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String jobId = "job123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = jobDashboardService.getPadDetails(request, jobId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetPadDetails_UnexpectedError() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Job job = new Job();
        job.setOrganizationId("org123");
        String jobId = "job123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.of(job));
        when(mongoTemplate.find(any(), eq(ActivityLogEntry.class), anyString()))
                .thenThrow(new RuntimeException("DB error"));

        // Act
        ResponseEntity<?> response = jobDashboardService.getPadDetails(request, jobId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testGetWellCompletionInformation_HappyPath() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Job job = new Job();
        job.setOrganizationId("org123");
        String jobId = "job123";
        String wellId = "well123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.of(job));

        // Act
        ResponseEntity<?> response = jobDashboardService.getWellCompletionInformation(request, jobId, wellId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetWellCompletionInformation_JobNotFound() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String jobId = "job123";
        String wellId = "well123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = jobDashboardService.getWellCompletionInformation(request, jobId, wellId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetCleanPerStage_HappyPath() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Job job = new Job();
        job.setOrganizationId("org123");
        String jobId = "job123";
        String wellId = "well123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.of(job));

        // Act
        ResponseEntity<?> response = jobDashboardService.getCleanPerStage(request, jobId, wellId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetCleanPerStage_JobNotFound() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String jobId = "job123";
        String wellId = "well123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = jobDashboardService.getCleanPerStage(request, jobId, wellId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetAveragePressureAndRate_HappyPath() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Job job = new Job();
        job.setOrganizationId("org123");
        String jobId = "job123";
        String wellId = "well123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.of(job));

        // Act
        ResponseEntity<?> response = jobDashboardService.getAveragePressureAndRate(request, jobId, wellId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetAveragePressureAndRate_JobNotFound() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String jobId = "job123";
        String wellId = "well123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = jobDashboardService.getAveragePressureAndRate(request, jobId, wellId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetFinalISIPAndFG_HappyPath() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Job job = new Job();
        job.setOrganizationId("org123");
        String jobId = "job123";
        String wellId = "well123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.of(job));

        // Act
        ResponseEntity<?> response = jobDashboardService.getFinalISIPAndFG(request, jobId, wellId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetFinalISIPAndFG_JobNotFound() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String jobId = "job123";
        String wellId = "well123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = jobDashboardService.getFinalISIPAndFG(request, jobId, wellId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetAverageVsMax_HappyPath() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Job job = new Job();
        job.setOrganizationId("org123");
        String jobId = "job123";
        String wellId = "well123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.of(job));

        // Act
        ResponseEntity<?> response = jobDashboardService.getAverageVsMax(request, jobId, wellId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetAverageVsMax_JobNotFound() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String jobId = "job123";
        String wellId = "well123";
        when(jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, "org123"))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = jobDashboardService.getAverageVsMax(request, jobId, wellId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}