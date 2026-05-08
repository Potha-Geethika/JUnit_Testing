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

    private DeletedActivityLogEntry activityLogEntry;

    @BeforeEach
    void setUp() {
        activityLogEntry = new DeletedActivityLogEntry();
        activityLogEntry.setId("1");
        activityLogEntry.setJobId("job1");
        activityLogEntry.setWell("well1");
        activityLogEntry.setStage(1.0f);
        activityLogEntry.setOrganizationId("org1");
        activityLogEntry.setDay(1);
    }

    @Test
    void testGetByOrganizationId_HappyPath() {
        when(deletedActivityLogMongoDbRepository.findByOrganizationId("org1"))
            .thenReturn(Collections.singletonList(activityLogEntry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.getByOrganizationId("org1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    void testGetByOrganizationId_EmptyList() {
        when(deletedActivityLogMongoDbRepository.findByOrganizationId("org2"))
            .thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.getByOrganizationId("org2");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetActivityLog_HappyPath() {
        when(deletedActivityLogMongoDbRepository.findById("1"))
            .thenReturn(Optional.of(activityLogEntry));

        Optional<DeletedActivityLogEntry> result = deletedActivityLogService.getActivityLog("1");

        assertNotNull(result);
        assertEquals("1", result.get().getId());
    }

    @Test
    void testGetActivityLog_NotFound() {
        when(deletedActivityLogMongoDbRepository.findById("2"))
            .thenReturn(Optional.empty());

        Optional<DeletedActivityLogEntry> result = deletedActivityLogService.getActivityLog("2");

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testFindByOrganizationIdAndJobId_HappyPath() {
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobId("org1", "job1"))
            .thenReturn(Collections.singletonList(activityLogEntry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobId("org1", "job1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    void testFindByOrganizationIdAndJobId_EmptyList() {
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobId("org1", "job2"))
            .thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobId("org1", "job2");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndWellAndStage_HappyPath() {
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWellAndStage("org1", "job1", "well1", 1.0f))
            .thenReturn(Collections.singletonList(activityLogEntry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndWellAndStage("org1", "job1", "well1", 1.0f);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndWellAndStage_EmptyList() {
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWellAndStage("org1", "job1", "well2", 1.0f))
            .thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndWellAndStage("org1", "job1", "well2", 1.0f);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndDay_HappyPath() {
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndDay("org1", "job1", 1))
            .thenReturn(Collections.singletonList(activityLogEntry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndDay("org1", "job1", 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndDay_EmptyList() {
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndDay("org1", "job1", 2))
            .thenReturn(Collections.emptyList());

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndDay("org1", "job1", 2);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testSaveActivityLog() {
        when(deletedActivityLogMongoDbRepository.save(activityLogEntry)).thenReturn(activityLogEntry);

        DeletedActivityLogEntry result = deletedActivityLogService.saveActivityLog(activityLogEntry);

        assertNotNull(result);
        assertEquals("1", result.getId());
    }

    @Test
    void testUpdateActivityLog() {
        doNothing().when(deletedActivityLogMongoDbRepository).save(activityLogEntry);

        deletedActivityLogService.updateActivityLog(activityLogEntry);

        verify(deletedActivityLogMongoDbRepository, times(1)).save(activityLogEntry);
    }

    @Test
    void testDeleteActivityLog() {
        doNothing().when(deletedActivityLogMongoDbRepository).deleteById("1");

        deletedActivityLogService.deleteActivityLog("1");

        verify(deletedActivityLogMongoDbRepository, times(1)).deleteById("1");
    }
}