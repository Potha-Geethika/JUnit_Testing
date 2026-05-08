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

    @BeforeEach
    void setUp() {
        activityLogService = new ActivityLogService(activityLogRepository, mongoTemplate, deletedActivityLogService,
                jobMongoDbRepository, pumpIssueMongoDbRepository, pendingMaintenanceEntryMongoDbRepository, organizationMongoDbRepository);
    }

    @Test
    void testGetByOrganizationId_HappyPath() {
        String organizationId = "org1";
        List<ActivityLogEntry> expectedList = Collections.singletonList(new ActivityLogEntry());
        when(activityLogRepository.findByOrganizationId(organizationId)).thenReturn(expectedList);

        List<ActivityLogEntry> result = activityLogService.getByOrganizationId(organizationId);
        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
    }

    @Test
    void testGetActivityLog_HappyPath() {
        String activityLogId = "log1";
        ActivityLogEntry expectedEntry = new ActivityLogEntry();
        when(activityLogRepository.findById(activityLogId)).thenReturn(Optional.of(expectedEntry));

        Optional<ActivityLogEntry> result = activityLogService.getActivityLog(activityLogId);
        assertTrue(result.isPresent());
        assertEquals(expectedEntry, result.get());
    }


    @Test
    void testSaveActivityLog_HappyPath() {
        ActivityLogEntry activityLog = new ActivityLogEntry();
        activityLog.setJobId("job1");
        activityLog.setOrganizationId("org1");
        when(jobMongoDbRepository.findById(activityLog.getJobId())).thenReturn(Optional.of(new Job()));
        when(activityLogRepository.save(activityLog)).thenReturn(activityLog);

        ActivityLogEntry result = activityLogService.saveActivityLog(activityLog);
        assertNotNull(result);
    }


    @Test
    void testDeleteActivityLog_HappyPath() {
        String activityLogId = "log1";
        doNothing().when(activityLogRepository).deleteById(activityLogId);

        activityLogService.deleteActivityLog(activityLogId);
        verify(activityLogRepository).deleteById(activityLogId);
    }




    @Test
    void testGetPumpTimeHistory_HappyPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String jobId = "job1";
        String eventOrNptCode = "Pump Time";
        when(activityLogRepository.findByJobIdAndEventOrNptCode(jobId, eventOrNptCode)).thenReturn(Collections.emptyList());
        
        ResponseEntity result = activityLogService.getPumpTimeHistory(request, jobId, eventOrNptCode);
        assertNotNull(result);
    }

    @Test
    void testGetActivityAndNptHistory_HappyPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String jobId = "job1";
        String activityOrNptCode = "Pump Time";
        when(activityLogRepository.findByJobId(jobId)).thenReturn(Collections.emptyList());

        ResponseEntity result = activityLogService.getActivityAndNptHistory(request, jobId, activityOrNptCode);
        assertNotNull(result);
    }
}