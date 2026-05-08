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

    @InjectMocks
    private DeletedActivityLogService deletedActivityLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetByOrganizationId_HappyPath() {
        String organizationId = "org123";
        DeletedActivityLogEntry entry = new DeletedActivityLogEntry();
        entry.setOrganizationId(organizationId);
        when(deletedActivityLogMongoDbRepository.findByOrganizationId(organizationId)).thenReturn(Collections.singletonList(entry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.getByOrganizationId(organizationId);
        assertEquals(1, result.size());
        assertEquals(organizationId, result.get(0).getOrganizationId());
    }

    @Test
    void testGetByOrganizationId_EmptyList() {
        String organizationId = "org123";
        when(deletedActivityLogMongoDbRepository.findByOrganizationId(organizationId)).thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.getByOrganizationId(organizationId);
        assertEquals(0, result.size());
    }

    @Test
    void testGetActivityLog_HappyPath() {
        String activityLogId = "log123";
        DeletedActivityLogEntry entry = new DeletedActivityLogEntry();
        entry.setId(activityLogId);
        when(deletedActivityLogMongoDbRepository.findById(activityLogId)).thenReturn(Optional.of(entry));

        Optional<DeletedActivityLogEntry> result = deletedActivityLogService.getActivityLog(activityLogId);
        assertEquals(entry, result.get());
    }

    @Test
    void testGetActivityLog_NotFound() {
        String activityLogId = "log123";
        when(deletedActivityLogMongoDbRepository.findById(activityLogId)).thenReturn(Optional.empty());

        Optional<DeletedActivityLogEntry> result = deletedActivityLogService.getActivityLog(activityLogId);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testFindByOrganizationIdAndJobId_HappyPath() {
        String organizationId = "org123";
        String jobId = "job123";
        DeletedActivityLogEntry entry = new DeletedActivityLogEntry();
        entry.setJobId(jobId);
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobId(organizationId, jobId)).thenReturn(Collections.singletonList(entry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobId(organizationId, jobId);
        assertEquals(1, result.size());
        assertEquals(jobId, result.get(0).getJobId());
    }

    @Test
    void testFindByOrganizationIdAndJobId_EmptyList() {
        String organizationId = "org123";
        String jobId = "job123";
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobId(organizationId, jobId)).thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobId(organizationId, jobId);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndWellAndStage_HappyPath() {
        String organizationId = "org123";
        String jobId = "job123";
        String well = "well1";
        Float stage = 1.0f;
        DeletedActivityLogEntry entry = new DeletedActivityLogEntry();
        entry.setWell(well);
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage)).thenReturn(Collections.singletonList(entry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage);
        assertEquals(1, result.size());
        assertEquals(well, result.get(0).getWell());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndWellAndStage_EmptyList() {
        String organizationId = "org123";
        String jobId = "job123";
        String well = "well1";
        Float stage = 1.0f;
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage)).thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndDay_HappyPath() {
        String organizationId = "org123";
        String jobId = "job123";
        Integer day = 1;
        DeletedActivityLogEntry entry = new DeletedActivityLogEntry();
        entry.setDay(day);
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day)).thenReturn(Collections.singletonList(entry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day);
        assertEquals(1, result.size());
        assertEquals(day, result.get(0).getDay());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndDay_EmptyList() {
        String organizationId = "org123";
        String jobId = "job123";
        Integer day = 1;
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day)).thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day);
        assertEquals(0, result.size());
    }

    @Test
    void testSaveActivityLog_HappyPath() {
        DeletedActivityLogEntry entry = new DeletedActivityLogEntry();
        when(deletedActivityLogMongoDbRepository.save(entry)).thenReturn(entry);

        DeletedActivityLogEntry result = deletedActivityLogService.saveActivityLog(entry);
        assertEquals(entry, result);
    }

    @Test
    void testUpdateActivityLog_HappyPath() {
        DeletedActivityLogEntry entry = new DeletedActivityLogEntry();
        doNothing().when(deletedActivityLogMongoDbRepository).save(entry);

        deletedActivityLogService.updateActivityLog(entry);
        verify(deletedActivityLogMongoDbRepository, times(1)).save(entry);
    }

    @Test
    void testDeleteActivityLog_HappyPath() {
        String activityLogId = "log123";
        doNothing().when(deletedActivityLogMongoDbRepository).deleteById(activityLogId);

        deletedActivityLogService.deleteActivityLog(activityLogId);
        verify(deletedActivityLogMongoDbRepository, times(1)).deleteById(activityLogId);
    }
}