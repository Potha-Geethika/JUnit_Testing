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





class ActivityLogEntryTest {

    @Test
    void testGetMillisecondsSpan_HappyPath() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setStart("12:00");
        entry.setEnd("13:00");
        long span = entry.getMillisecondsSpan();
        Assertions.assertEquals(3600000, span);
    }

    @Test
    void testGetMillisecondsSpan_ZeroDuration() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setStart("12:00");
        entry.setEnd("12:00");
        long span = entry.getMillisecondsSpan();
        Assertions.assertEquals(0, span);
    }

    @Test
    void testGetMillisecondsSpan_NullStart() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setEnd("13:00");
        long span = entry.getMillisecondsSpan();
        Assertions.assertEquals(0, span);
    }

    @Test
    void testGetMillisecondsSpan_NullEnd() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setStart("12:00");
        long span = entry.getMillisecondsSpan();
        Assertions.assertEquals(0, span);
    }

    @Test
    void testGetMillisecondsSpan_StartAfterEnd() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setStart("14:00");
        entry.setEnd("13:00");
        Assertions.assertThrows(IllegalStateException.class, entry::getMillisecondsSpan);
    }

    @Test
    void testCompareTo() {
        ActivityLogEntry entry1 = new ActivityLogEntry();
        entry1.setDay(1);
        entry1.setStart("12:00");
        
        ActivityLogEntry entry2 = new ActivityLogEntry();
        entry2.setDay(1);
        entry2.setStart("13:00");
        
        Assertions.assertTrue(entry1.compareTo(entry2) < 0);
    }

    @Test
    void testGetComplete() {
        ActivityLogEntry entry = new ActivityLogEntry();
        
        entry.setComplete(true);
        Assertions.assertTrue(entry.getComplete());
        
        entry.setComplete(false);
        Assertions.assertFalse(entry.getComplete());
        
        entry.setComplete(null);
        Assertions.assertFalse(entry.getComplete());
    }

    @Test
    void testGetDate() {
        ActivityLogEntry entry = new ActivityLogEntry();
        Date date = new Date();
        entry.setDate(date);
        Assertions.assertNotNull(entry.getDate());
        Assertions.assertEquals(date, entry.getDate());
    }

    @Test
    void testSetAndGetId() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setId("123");
        Assertions.assertEquals("123", entry.getId());
    }

    @Test
    void testSetAndGetJobId() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setJobId("job123");
        Assertions.assertEquals("job123", entry.getJobId());
    }

    @Test
    void testSetAndGetOrganizationId() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setOrganizationId("org123");
        Assertions.assertEquals("org123", entry.getOrganizationId());
    }
}