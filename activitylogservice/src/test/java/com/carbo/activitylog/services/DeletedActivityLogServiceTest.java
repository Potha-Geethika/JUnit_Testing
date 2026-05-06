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
    }

    @Test
    void testGetByOrganizationId() {
        String organizationId = "org1";
        when(deletedActivityLogMongoDbRepository.findByOrganizationId(organizationId)).thenReturn(Collections.singletonList(activityLogEntry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.getByOrganizationId(organizationId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activityLogEntry.getId(), result.get(0).getId());
    }

    @Test
    void testGetActivityLog() {
        String activityLogId = "1";
        when(deletedActivityLogMongoDbRepository.findById(activityLogId)).thenReturn(Optional.of(activityLogEntry));

        Optional<DeletedActivityLogEntry> result = deletedActivityLogService.getActivityLog(activityLogId);

        assertNotNull(result);
        assertEquals(activityLogEntry.getId(), result.get().getId());
    }

    @Test
    void testFindByOrganizationIdAndJobId() {
        String organizationId = "org1";
        String jobId = "job1";
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobId(organizationId, jobId)).thenReturn(Collections.singletonList(activityLogEntry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobId(organizationId, jobId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activityLogEntry.getId(), result.get(0).getId());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndWellAndStage() {
        String organizationId = "org1";
        String jobId = "job1";
        String well = "well1";
        Float stage = 1.0f;
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage)).thenReturn(Collections.singletonList(activityLogEntry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activityLogEntry.getId(), result.get(0).getId());
    }

    @Test
    void testFindByOrganizationIdAndJobIdAndDay() {
        String organizationId = "org1";
        String jobId = "job1";
        Integer day = 1;
        when(deletedActivityLogMongoDbRepository.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day)).thenReturn(Collections.singletonList(activityLogEntry));

        List<DeletedActivityLogEntry> result = deletedActivityLogService.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activityLogEntry.getId(), result.get(0).getId());
    }

    @Test
    void testSaveActivityLog() {
        when(deletedActivityLogMongoDbRepository.save(activityLogEntry)).thenReturn(activityLogEntry);

        DeletedActivityLogEntry result = deletedActivityLogService.saveActivityLog(activityLogEntry);

        assertNotNull(result);
        assertEquals(activityLogEntry.getId(), result.getId());
    }

    @Test
    void testUpdateActivityLog() {
        doNothing().when(deletedActivityLogMongoDbRepository).save(activityLogEntry);

        deletedActivityLogService.updateActivityLog(activityLogEntry);

        verify(deletedActivityLogMongoDbRepository, times(1)).save(activityLogEntry);
    }

    @Test
    void testDeleteActivityLog() {
        String activityLogId = "1";
        doNothing().when(deletedActivityLogMongoDbRepository).deleteById(activityLogId);

        deletedActivityLogService.deleteActivityLog(activityLogId);

        verify(deletedActivityLogMongoDbRepository, times(1)).deleteById(activityLogId);
    }
}