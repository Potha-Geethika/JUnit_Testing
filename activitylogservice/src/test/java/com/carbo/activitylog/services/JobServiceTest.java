package com.carbo.activitylog.services;

import com.carbo.activitylog.model.Job;
import com.carbo.activitylog.repository.JobMongoDbRepository;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.*;
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
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;






@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobMongoDbRepository jobMongoDbRepository;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobMongoDbRepository);
    }

    @Test
    void testGetByOrganizationId_HappyPath() {
        String organizationId = "org-123";
        List<Job> expectedJobs = Collections.singletonList(new Job());
        when(jobMongoDbRepository.findByOrganizationId(organizationId)).thenReturn(expectedJobs);

        List<Job> result = jobService.getByOrganizationId(organizationId);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetByOrganizationId_EmptyList() {
        String organizationId = "org-123";
        when(jobMongoDbRepository.findByOrganizationId(organizationId)).thenReturn(Collections.emptyList());

        List<Job> result = jobService.getByOrganizationId(organizationId);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetByOrganizationIdAndJobId_HappyPath() {
        String organizationId = "org-123";
        String jobId = "job-456";
        Job expectedJob = new Job();
        expectedJob.setId(jobId);
        when(jobMongoDbRepository.findByOrganizationIdAndId(organizationId, jobId)).thenReturn(Collections.singletonList(expectedJob));

        Optional<Job> result = jobService.getByOrganizationIdAndJobId(organizationId, jobId);
        assertNotNull(result);
        assertEquals(expectedJob.getId(), result.get().getId());
    }

    @Test
    void testGetByOrganizationIdAndJobId_JobNotFound() {
        String organizationId = "org-123";
        String jobId = "job-456";
        when(jobMongoDbRepository.findByOrganizationIdAndId(organizationId, jobId)).thenReturn(Collections.emptyList());

        Optional<Job> result = jobService.getByOrganizationIdAndJobId(organizationId, jobId);
        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testGetJobByJobNumber_HappyPath() {
        String jobNumber = "job-789";
        List<Job> expectedJobs = Collections.singletonList(new Job());
        when(jobMongoDbRepository.findByJobNumber(jobNumber)).thenReturn(expectedJobs);

        List<Job> result = jobService.getJobByJobNumber(jobNumber);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetJobByJobNumber_EmptyList() {
        String jobNumber = "job-789";
        when(jobMongoDbRepository.findByJobNumber(jobNumber)).thenReturn(Collections.emptyList());

        List<Job> result = jobService.getJobByJobNumber(jobNumber);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByJobId_HappyPath() {
        String jobId = "job-456";
        Job expectedJob = new Job();
        expectedJob.setId(jobId);
        when(jobMongoDbRepository.findById(jobId)).thenReturn(Optional.of(expectedJob));

        Optional<Job> result = jobService.findByJobId(jobId);
        assertNotNull(result);
        assertEquals(expectedJob.getId(), result.get().getId());
    }

    @Test
    void testFindByJobId_JobNotFound() {
        String jobId = "job-456";
        when(jobMongoDbRepository.findById(jobId)).thenReturn(Optional.empty());

        Optional<Job> result = jobService.findByJobId(jobId);
        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }
}