package com.carbo.activitylog.services;
import static org.mockito.ArgumentMatchers.anyString;
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

    private Job job;
    private ActivityLogEntry activityLogEntry;







    @Test
    void testDeleteActivityLog_HappyPath() {
        doNothing().when(activityLogRepository).deleteById(activityLogEntry.getId());
        activityLogService.deleteActivityLog(activityLogEntry.getId());
        verify(activityLogRepository, times(1)).deleteById(activityLogEntry.getId());
    }


    @Test
    void testCopyActivityLogs_HappyPath() {
        ActivityLogCopyPayload copyPayload = new ActivityLogCopyPayload("jobId", 1, 2);
        List<ActivityLogEntry> entries = new ArrayList<>();
        entries.add(activityLogEntry);

        when(activityLogRepository.findByJobIdAndDay(copyPayload.getJobId(), copyPayload.getFromDay())).thenReturn(entries);
        when(activityLogRepository.existsByJobIdAndDay(copyPayload.getJobId(), copyPayload.getFromDay())).thenReturn(false);
        when(activityLogRepository.saveAll(anyList())).thenReturn(entries);

        ResponseEntity response = activityLogService.copyActivityLogs(copyPayload);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ErrorConstants.COPY_SUCCESSFULLY_CODE, ((Success) response.getBody()).getCode());
    }

    @Test
    void testCopyActivityLogs_ActivityAlreadyExists() {
        ActivityLogCopyPayload copyPayload = new ActivityLogCopyPayload("jobId", 1, 2);
        List<ActivityLogEntry> entries = new ArrayList<>();
        entries.add(activityLogEntry);

        when(activityLogRepository.findByJobIdAndDay(copyPayload.getJobId(), copyPayload.getFromDay())).thenReturn(entries);
        when(activityLogRepository.existsByJobIdAndDay(copyPayload.getJobId(), 2)).thenReturn(true);

        ResponseEntity response = activityLogService.copyActivityLogs(copyPayload);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorConstants.ERROR_WHILE_ACTIVITY_ALREADY_EXIST_CODE, ((Error) response.getBody()).getErrorCode());
    }

    @Test
    void testFetchMismatchedActivityLogs_HappyPath() throws IOException {
        String requestedOrganizationId = "orgId";
        String startDateTime = "2024-11-18T14:30:00Z";
        String endDateTime = "2024-11-30T14:30:00Z";
        boolean sendEmail = false;

        when(activityLogRepository.getSimplifiedActivitiesForMismatchEntriesAndCreatedRange(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        MismatchActivityLogsResponse response = activityLogService.fetchMismatchedActivityLogs(requestedOrganizationId, startDateTime, endDateTime, sendEmail);
        assertNotNull(response);
        assertEquals(0, response.getTotalCount());
    }
}