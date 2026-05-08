package com.carbo.activitylog.services;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.activitylog.controllers.SyncController;
import com.carbo.activitylog.exception.ErrorException;
import com.carbo.activitylog.model.*;
import com.carbo.activitylog.model.error.Error;
import com.carbo.activitylog.model.error.Success;
import com.carbo.activitylog.repository.*;
import com.carbo.activitylog.utils.Constants;
import com.carbo.activitylog.utils.ErrorConstants;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import static com.carbo.activitylog.utils.ActivityLogUtil.convertToLocalDateTime;
import static com.carbo.activitylog.utils.CommonUtils.*;
import static com.carbo.activitylog.utils.Constants.*;
import static com.carbo.activitylog.utils.ControllerUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;







@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogMongoDbRepository activityLogRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private DeletedActivityLogService deletedActivityLogService;

    @Mock
    private JobMongoDbRepository jobMongoDbRepository;

    @Mock
    private PumpIssueMongoDbRepository pumpIssueMongoDbRepository;

    @Mock
    private PendingMaintenanceEntryMongoDbRepository pendingMaintenanceEntryMongoDbRepository;

    @Mock
    private OrganizationMongoDbRepository organizationMongoDbRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    private ActivityLogEntry activityLogEntry;

    @BeforeEach
    void setUp() {
        activityLogEntry = new ActivityLogEntry();
        activityLogEntry.setId(UUID.randomUUID().toString());
        activityLogEntry.setJobId(UUID.randomUUID().toString());
        activityLogEntry.setOrganizationId(UUID.randomUUID().toString());
        activityLogEntry.setDay(1);
        activityLogEntry.setWell("Well1");
        activityLogEntry.setStart(System.currentTimeMillis());
        activityLogEntry.setEnd(System.currentTimeMillis() + 1000);
        activityLogEntry.setStage(1.0f);
    }

    @Test
    void testSaveActivityLog_HappyPath() {
        when(jobMongoDbRepository.findById(activityLogEntry.getJobId())).thenReturn(Optional.of(new Job()));
        when(activityLogRepository.save(any(ActivityLogEntry.class))).thenReturn(activityLogEntry);

        ActivityLogEntry savedLog = activityLogService.saveActivityLog(activityLogEntry);

        assertNotNull(savedLog);
        assertEquals(activityLogEntry.getId(), savedLog.getId());
    }

    @Test
    void testSaveActivityLog_JobNotFound() {
        when(jobMongoDbRepository.findById(activityLogEntry.getJobId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            activityLogService.saveActivityLog(activityLogEntry);
        });

        assertEquals(Constants.ERROR_JOB_DOES_NOT_EXISTS_FOR_ACTIVITY, exception.getMessage());
    }

    @Test
    void testUpdateActivityLog_HappyPath() {
        when(jobMongoDbRepository.findById(activityLogEntry.getJobId())).thenReturn(Optional.of(new Job()));
        when(activityLogRepository.findById(activityLogEntry.getId())).thenReturn(Optional.of(activityLogEntry));

        activityLogService.updateActivityLog(activityLogEntry);

        verify(activityLogRepository, times(1)).save(activityLogEntry);
    }

    @Test
    void testUpdateActivityLog_JobNotFound() {
        when(jobMongoDbRepository.findById(activityLogEntry.getJobId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            activityLogService.updateActivityLog(activityLogEntry);
        });

        assertEquals(Constants.ERROR_JOB_DOES_NOT_EXISTS_FOR_ACTIVITY, exception.getMessage());
    }

    @Test
    void testDeleteActivityLog_HappyPath() {
        doNothing().when(activityLogRepository).deleteById(activityLogEntry.getId());

        activityLogService.deleteActivityLog(activityLogEntry.getId());

        verify(activityLogRepository, times(1)).deleteById(activityLogEntry.getId());
    }

    @Test
    void testValidateAndStoreActivityLog_HappyPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(activityLogRepository.findById(activityLogEntry.getId())).thenReturn(Optional.of(activityLogEntry));
        when(deletedActivityLogService.saveActivityLog(any(DeletedActivityLogEntry.class))).thenReturn(new DeletedActivityLogEntry());

        boolean result = activityLogService.validateAndStoreActivityLog(request, activityLogEntry.getId());

        assertTrue(result);
    }

    @Test
    void testValidateAndStoreActivityLog_NotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(activityLogRepository.findById(activityLogEntry.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ErrorException.class, () -> {
            activityLogService.validateAndStoreActivityLog(request, activityLogEntry.getId());
        });

        assertEquals(ErrorConstants.NO_ACTIVITY_LOG_FOUND_ERROR_CODE, exception.getError().getErrorCode());
    }

    @Test
    void testCopyActivityLogs_HappyPath() {
        ActivityLogCopyPayload copyPayload = new ActivityLogCopyPayload();
        copyPayload.setJobId(activityLogEntry.getJobId());
        copyPayload.setFromDay(1);
        copyPayload.setToDay(2);
        
        when(activityLogRepository.findByJobIdAndDay(any(), any())).thenReturn(new ArrayList<>(Collections.singletonList(activityLogEntry)));

        ResponseEntity response = activityLogService.copyActivityLogs(copyPayload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ErrorConstants.COPY_SUCCESSFULLY_CODE, ((Success) response.getBody()).getCode());
    }

    @Test
    void testCopyActivityLogs_ActivityComplete() {
        ActivityLogCopyPayload copyPayload = new ActivityLogCopyPayload();
        copyPayload.setJobId(activityLogEntry.getJobId());
        copyPayload.setFromDay(1);
        copyPayload.setToDay(2);
        
        when(activityLogRepository.findByJobIdAndDay(any(), any())).thenReturn(new ArrayList<>(Collections.singletonList(activityLogEntry)));
        when(activityLogRepository.existsByJobIdAndDay(any(), any())).thenReturn(true);

        ResponseEntity response = activityLogService.copyActivityLogs(copyPayload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorConstants.ERROR_WHILE_ACTIVITY_COMPLETE_CODE, ((Error) response.getBody()).getErrorCode());
    }
}