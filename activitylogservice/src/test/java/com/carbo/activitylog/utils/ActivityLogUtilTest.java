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
        assertEquals(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), date.toInstant());
    }

    @Test
    void testToLocalDateFromLong() {
        Long timestamp = new Date().getTime();
        LocalDate localDate = ActivityLogUtil.toLocalDate(timestamp);
        assertNotNull(localDate);
        assertEquals(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), Instant.ofEpochMilli(timestamp));
    }

    @Test
    void testConvertToLocalDateTimeWithValidHHMM() {
        String time = "12:30";
        LocalDateTime localDateTime = ActivityLogUtil.convertToLocalDateTime(time);
        assertNotNull(localDateTime);
        assertEquals(12, localDateTime.getHour());
        assertEquals(30, localDateTime.getMinute());
    }

    @Test
    void testConvertToLocalDateTimeWithValidFullFormat() {
        String time = "20230101 12:30";
        LocalDateTime localDateTime = ActivityLogUtil.convertToLocalDateTime(time);
        assertNotNull(localDateTime);
        assertEquals(2023, localDateTime.getYear());
        assertEquals(1, localDateTime.getMonthValue());
        assertEquals(1, localDateTime.getDayOfMonth());
        assertEquals(12, localDateTime.getHour());
        assertEquals(30, localDateTime.getMinute());
    }

    @Test
    void testConvertToLocalDateTimeWithNull() {
        LocalDateTime localDateTime = ActivityLogUtil.convertToLocalDateTime(null);
        assertNull(localDateTime);
    }

    @Test
    void testGetTotalPumpTimeInMinsWithEmptyList() {
        Float totalPumpTime = ActivityLogUtil.getTotalPumpTimeInMins(Collections.emptyList());
        assertEquals(0.0f, totalPumpTime);
    }

    @Test
    void testGetTotalPumpTimeInMinsWithValidEntries() {
        ActivityLogEntry entry1 = new ActivityLogEntry();
        entry1.setStart("12:00");
        entry1.setEnd("12:30");

        ActivityLogEntry entry2 = new ActivityLogEntry();
        entry2.setStart("13:00");
        entry2.setEnd("13:15");

        Float totalPumpTime = ActivityLogUtil.getTotalPumpTimeInMins(Arrays.asList(entry1, entry2));
        assertNotNull(totalPumpTime);
        assertEquals(45.0f, totalPumpTime);
    }

    @Test
    void testFormatTimeWithDate() {
        String hhMM = "12:30";
        ZonedDateTime date = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedTime = ActivityLogUtil.formatTimeWithDate(hhMM, date, formatter);
        assertNotNull(formattedTime);
        assertTrue(formattedTime.contains(date.toLocalDate().toString()));
    }
}