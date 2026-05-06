package com.carbo.activitylog.utils;

import com.carbo.activitylog.model.ActivityLogEntry;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;






class ActivityLogUtilTest {

    @Test
    void testToLocalDateFromDate() {
        Date date = new Date();
        LocalDate localDate = ActivityLogUtil.toLocalDate(date, ZoneId.systemDefault());
        assertNotNull(localDate);
    }

    @Test
    void testToLocalDateFromLong() {
        long timestamp = System.currentTimeMillis();
        LocalDate localDate = ActivityLogUtil.toLocalDate(timestamp);
        assertNotNull(localDate);
    }

    @Test
    void testConvertToLocalDateTimeHappyPath() {
        String time = "12:30";
        LocalDateTime localDateTime = ActivityLogUtil.convertToLocalDateTime(time);
        assertNotNull(localDateTime);
    }

    @Test
    void testConvertToLocalDateTimeWithDate() {
        String time = "20230101 12:30";
        LocalDateTime localDateTime = ActivityLogUtil.convertToLocalDateTime(time);
        assertNotNull(localDateTime);
    }

    @Test
    void testConvertToLocalDateTimeNullInput() {
        LocalDateTime localDateTime = ActivityLogUtil.convertToLocalDateTime(null);
        assertNull(localDateTime);
    }

    @Test
    void testGetTotalPumpTimeInMinsHappyPath() {
        List<ActivityLogEntry> entries = new ArrayList<>();
        ActivityLogEntry entry1 = Mockito.mock(ActivityLogEntry.class);
        ActivityLogEntry entry2 = Mockito.mock(ActivityLogEntry.class);
        Mockito.when(entry1.getMillisecondsSpan()).thenReturn(60000L); // 1 minute
        Mockito.when(entry2.getMillisecondsSpan()).thenReturn(120000L); // 2 minutes
        entries.add(entry1);
        entries.add(entry2);
        
        Float totalPumpTime = ActivityLogUtil.getTotalPumpTimeInMins(entries);
        assertEquals(3.0f, totalPumpTime);
    }

    @Test
    void testGetTotalPumpTimeInMinsEmptyList() {
        List<ActivityLogEntry> entries = Collections.emptyList();
        Float totalPumpTime = ActivityLogUtil.getTotalPumpTimeInMins(entries);
        assertEquals(0.0f, totalPumpTime);
    }

    @Test
    void testFormatTimeWithDate() {
        String hhMM = "12:30";
        ZonedDateTime date = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedTime = ActivityLogUtil.formatTimeWithDate(hhMM, date, formatter);
        assertNotNull(formattedTime);
    }
}