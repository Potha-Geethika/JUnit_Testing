package com.carbo.activitylog.services;

import com.carbo.activitylog.model.DeletedActivityLogEntry;
import com.carbo.activitylog.repository.DeletedActivityLogMongoDbRepository;
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
class DeletedActivityLogServiceTest {

    @Mock
    private DeletedActivityLogMongoDbRepository deletedActivityLogMongoDbRepository;

    @InjectMocks
    private DeletedActivityLogService deletedActivityLogService;

    @BeforeEach
    void setUp() {
        // No additional setup needed for mocks
    }

    @Test
    void testGetByOrganizationId_HappyPath() {
        // Arrange
        String organizationId = "org1";
        List<DeletedActivityLogEntry> expectedList = Collections.singletonList(new DeletedActivityLogEntry());
        when(deletedActivityLogMongoDbRepository.findByOrganizationId(organizationId)).thenReturn(expectedList);

        // Act
        List<DeletedActivityLogEntry> result = deletedActivityLogService.getByOrganizationId(organizationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetActivityLog_HappyPath() {
        // Arrange
        String activityLogId = "log1";
        DeletedActivityLogEntry expectedEntry = new DeletedActivityLogEntry();
        when(deletedActivityLogMongoDbRepository.findById(activityLogId)).thenReturn(Optional.of(expectedEntry));

        // Act
        Optional<DeletedActivityLogEntry> result = deletedActivityLogService.getActivityLog(activityLogId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedEntry, result.get());
    }

    @Test
    void testFindByOrganizationIdAndJobId_HappyPath() {
        // Arrange
        String organizationId = "org1";
        String jobId = "job1";
        List<DeletedActivityLogEntry> expectedList = Collections.singletonList(new DeletedActivityLogEntry());
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobId(organizationId, jobId)).thenReturn(expectedList);

        // Act
        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobId(organizationId, jobId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndWellAndStage_HappyPath() {
        // Arrange
        String organizationId = "org1";
        String jobId = "job1";
        String well = "well1";
        Float stage = 1.0f;
        List<DeletedActivityLogEntry> expectedList = Collections.singletonList(new DeletedActivityLogEntry());
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage)).thenReturn(expectedList);

        // Act
        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndDay_HappyPath() {
        // Arrange
        String organizationId = "org1";
        String jobId = "job1";
        Integer day = 1;
        List<DeletedActivityLogEntry> expectedList = Collections.singletonList(new DeletedActivityLogEntry());
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day)).thenReturn(expectedList);

        // Act
        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testSaveActivityLog_HappyPath() {
        // Arrange
        DeletedActivityLogEntry activityLog = new DeletedActivityLogEntry();
        when(deletedActivityLogMongoDbRepository.save(activityLog)).thenReturn(activityLog);

        // Act
        DeletedActivityLogEntry result = deletedActivityLogService.saveActivityLog(activityLog);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testUpdateActivityLog_HappyPath() {
        // Arrange
        DeletedActivityLogEntry activityLog = new DeletedActivityLogEntry();
        when(deletedActivityLogMongoDbRepository.save(activityLog)).thenReturn(activityLog);

        // Act
        deletedActivityLogService.updateActivityLog(activityLog);

        // Assert
        // No exception should be thrown
    }

    @Test
    void testDeleteActivityLog_HappyPath() {
        // Arrange
        String activityLogId = "log1";

        // Act
        deletedActivityLogService.deleteActivityLog(activityLogId);

        // Assert
        // No exception should be thrown
    }
}