package com.carbo.pad.utils;

import com.carbo.pad.model.ActivityLogEntry;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.*;
import java.util.stream.Collectors;
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
    void testConvertToLocalDateTime_ValidTime() {
        LocalDateTime result = ActivityLogUtil.convertToLocalDateTime("12:30");
        assertNotNull(result);
        assertEquals(12, result.getHour());
        assertEquals(30, result.getMinute());
    }

    @Test
    void testConvertToLocalDateTime_ValidDateTime() {
        LocalDateTime result = ActivityLogUtil.convertToLocalDateTime("20230101 12:30");
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(12, result.getHour());
        assertEquals(30, result.getMinute());
    }

    @Test
    void testConvertToLocalDateTime_NullInput() {
        LocalDateTime result = ActivityLogUtil.convertToLocalDateTime(null);
        assertNull(result);
    }

    @Test
    void testGetTotalPumpTimeInMins_EmptyList() {
        Float result = ActivityLogUtil.getTotalPumpTimeInMins(Collections.emptyList());
        assertEquals(0.0f, result);
    }

    @Test
    void testGetTotalPumpTimeInMins_ValidEntries() {
        ActivityLogEntry entry1 = Mockito.mock(ActivityLogEntry.class);
        Mockito.when(entry1.getMillisecondsSpan()).thenReturn(60000L); // 1 minute
        ActivityLogEntry entry2 = Mockito.mock(ActivityLogEntry.class);
        Mockito.when(entry2.getMillisecondsSpan()).thenReturn(120000L); // 2 minutes

        List<ActivityLogEntry> entries = Arrays.asList(entry1, entry2);
        Float result = ActivityLogUtil.getTotalPumpTimeInMins(entries);
        assertEquals(3.0f, result);
    }

    @Test
    void testGetPumpTimeInMinsList_EmptyList() {
        List<Float> result = ActivityLogUtil.getPumpTimeInMinsList(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPumpTimeInMinsList_ValidEntries() {
        ActivityLogEntry entry1 = Mockito.mock(ActivityLogEntry.class);
        Mockito.when(entry1.getStage()).thenReturn(1.0f);
        Mockito.when(entry1.getMillisecondsSpan()).thenReturn(60000L); // 1 minute
        Mockito.when(entry1.getModified()).thenReturn(1L);

        ActivityLogEntry entry2 = Mockito.mock(ActivityLogEntry.class);
        Mockito.when(entry2.getStage()).thenReturn(1.0f);
        Mockito.when(entry2.getMillisecondsSpan()).thenReturn(120000L); // 2 minutes
        Mockito.when(entry2.getModified()).thenReturn(2L);

        List<ActivityLogEntry> entries = Arrays.asList(entry1, entry2);
        List<Float> result = ActivityLogUtil.getPumpTimeInMinsList(entries);
        assertEquals(1, result.size());
        assertEquals(2.0f, result.get(0));
    }

    @Test
    void testRound_ValidFloat() {
        Float result = ActivityLogUtil.round(2.56789f, 2);
        assertEquals(2.57f, result);
    }

    @Test
    void testRound_ValidDouble() {
        Double result = ActivityLogUtil.round(2.56789, 2);
        assertEquals(2.57, result);
    }
}