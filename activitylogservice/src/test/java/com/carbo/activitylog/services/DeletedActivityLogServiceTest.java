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






class DeletedActivityLogServiceTest {

    @Mock
    private DeletedActivityLogMongoDbRepository deletedActivityLogMongoDbRepository;

    private DeletedActivityLogService deletedActivityLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deletedActivityLogService = new DeletedActivityLogService(deletedActivityLogMongoDbRepository);
    }

    @Test
    void testGetByOrganizationId_HappyPath() {
        String organizationId = "org123";
        List<DeletedActivityLogEntry> expectedList = Collections.singletonList(new DeletedActivityLogEntry());
        when(deletedActivityLogMongoDbRepository.findByOrganizationId(organizationId)).thenReturn(expectedList);

        List<DeletedActivityLogEntry> result = deletedActivityLogService.getByOrganizationId(organizationId);

        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        verify(deletedActivityLogMongoDbRepository).findByOrganizationId(organizationId);
    }

    @Test
    void testGetByOrganizationId_EmptyList() {
        String organizationId = "org123";
        when(deletedActivityLogMongoDbRepository.findByOrganizationId(organizationId)).thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.getByOrganizationId(organizationId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(deletedActivityLogMongoDbRepository).findByOrganizationId(organizationId);
    }

    @Test
    void testGetActivityLog_HappyPath() {
        String activityLogId = "log123";
        DeletedActivityLogEntry expectedEntry = new DeletedActivityLogEntry();
        when(deletedActivityLogMongoDbRepository.findById(activityLogId)).thenReturn(Optional.of(expectedEntry));

        Optional<DeletedActivityLogEntry> result = deletedActivityLogService.getActivityLog(activityLogId);

        assertTrue(result.isPresent());
        assertEquals(expectedEntry, result.get());
        verify(deletedActivityLogMongoDbRepository).findById(activityLogId);
    }

    @Test
    void testGetActivityLog_NotFound() {
        String activityLogId = "log123";
        when(deletedActivityLogMongoDbRepository.findById(activityLogId)).thenReturn(Optional.empty());

        Optional<DeletedActivityLogEntry> result = deletedActivityLogService.getActivityLog(activityLogId);

        assertFalse(result.isPresent());
        verify(deletedActivityLogMongoDbRepository).findById(activityLogId);
    }

    @Test
    void testFindByOrganizationIdAndJobId_HappyPath() {
        String organizationId = "org123";
        String jobId = "job123";
        List<DeletedActivityLogEntry> expectedList = Collections.singletonList(new DeletedActivityLogEntry());
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobId(organizationId, jobId)).thenReturn(expectedList);

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobId(organizationId, jobId);

        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        verify(deletedActivityLogMongoDbRepository).findByOrganizationIdAndJobId(organizationId, jobId);
    }

    @Test
    void testFindByOrganizationIdAndJobId_EmptyList() {
        String organizationId = "org123";
        String jobId = "job123";
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobId(organizationId, jobId)).thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobId(organizationId, jobId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(deletedActivityLogMongoDbRepository).findByOrganizationIdAndJobId(organizationId, jobId);
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndWellAndStage_HappyPath() {
        String organizationId = "org123";
        String jobId = "job123";
        String well = "well1";
        Float stage = 1.0f;
        List<DeletedActivityLogEntry> expectedList = Collections.singletonList(new DeletedActivityLogEntry());
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage)).thenReturn(expectedList);

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage);

        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        verify(deletedActivityLogMongoDbRepository).findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage);
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndWellAndStage_EmptyList() {
        String organizationId = "org123";
        String jobId = "job123";
        String well = "well1";
        Float stage = 1.0f;
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage)).thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(deletedActivityLogMongoDbRepository).findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage);
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndDay_HappyPath() {
        String organizationId = "org123";
        String jobId = "job123";
        Integer day = 1;
        List<DeletedActivityLogEntry> expectedList = Collections.singletonList(new DeletedActivityLogEntry());
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day)).thenReturn(expectedList);

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day);

        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        verify(deletedActivityLogMongoDbRepository).findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day);
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndDay_EmptyList() {
        String organizationId = "org123";
        String jobId = "job123";
        Integer day = 1;
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day)).thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(deletedActivityLogMongoDbRepository).findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day);
    }

    @Test
    void testSaveActivityLog_HappyPath() {
        DeletedActivityLogEntry activityLogEntry = new DeletedActivityLogEntry();
        when(deletedActivityLogMongoDbRepository.save(activityLogEntry)).thenReturn(activityLogEntry);

        DeletedActivityLogEntry result = deletedActivityLogService.saveActivityLog(activityLogEntry);

        assertNotNull(result);
        assertEquals(activityLogEntry, result);
        verify(deletedActivityLogMongoDbRepository).save(activityLogEntry);
    }

    @Test
    void testUpdateActivityLog_HappyPath() {
        DeletedActivityLogEntry activityLogEntry = new DeletedActivityLogEntry();
        doNothing().when(deletedActivityLogMongoDbRepository).save(activityLogEntry);

        deletedActivityLogService.updateActivityLog(activityLogEntry);

        verify(deletedActivityLogMongoDbRepository).save(activityLogEntry);
    }

    @Test
    void testDeleteActivityLog_HappyPath() {
        String activityLogId = "log123";
        doNothing().when(deletedActivityLogMongoDbRepository).deleteById(activityLogId);

        deletedActivityLogService.deleteActivityLog(activityLogId);

        verify(deletedActivityLogMongoDbRepository).deleteById(activityLogId);
    }
}