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

    @InjectMocks
    private JobService jobService;

    private Job job;

    @BeforeEach
    void setUp() {
        job = new Job();
        job.setId("1");
        job.setName("Test Job");
        job.setJobNumber("J123");
        job.setOrganizationId("Org1");
    }

    @Test
    void getByOrganizationId_HappyPath() {
        when(jobMongoDbRepository.findByOrganizationId("Org1")).thenReturn(Collections.singletonList(job));

        List<Job> result = jobService.getByOrganizationId("Org1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    void getByOrganizationId_EmptyList() {
        when(jobMongoDbRepository.findByOrganizationId("Org1")).thenReturn(Collections.emptyList());

        List<Job> result = jobService.getByOrganizationId("Org1");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getByOrganizationIdAndJobId_HappyPath() {
        when(jobMongoDbRepository.findByOrganizationIdAndId("Org1", "1")).thenReturn(Collections.singletonList(job));

        Optional<Job> result = jobService.getByOrganizationIdAndJobId("Org1", "1");

        assertNotNull(result);
        assertEquals("1", result.get().getId());
    }

    @Test
    void getByOrganizationIdAndJobId_JobNotFound() {
        when(jobMongoDbRepository.findByOrganizationIdAndId("Org1", "2")).thenReturn(Collections.emptyList());

        Optional<Job> result = jobService.getByOrganizationIdAndJobId("Org1", "2");

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void getJobByJobNumber_HappyPath() {
        when(jobMongoDbRepository.findByJobNumber("J123")).thenReturn(Collections.singletonList(job));

        List<Job> result = jobService.getJobByJobNumber("J123");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    void getJobByJobNumber_EmptyList() {
        when(jobMongoDbRepository.findByJobNumber("J999")).thenReturn(Collections.emptyList());

        List<Job> result = jobService.getJobByJobNumber("J999");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void findByJobId_HappyPath() {
        when(jobMongoDbRepository.findById("1")).thenReturn(Optional.of(job));

        Optional<Job> result = jobService.findByJobId("1");

        assertNotNull(result);
        assertEquals("1", result.get().getId());
    }

    @Test
    void findByJobId_JobNotFound() {
        when(jobMongoDbRepository.findById("2")).thenReturn(Optional.empty());

        Optional<Job> result = jobService.findByJobId("2");

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }
}