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
    void testGetMillisecondsSpan() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setStart("12:00");
        entry.setEnd("13:00");
        Assertions.assertEquals(3600000, entry.getMillisecondsSpan());

        entry.setStart("13:00");
        entry.setEnd("12:00");
        Assertions.assertThrows(IllegalStateException.class, entry::getMillisecondsSpan);

        entry.setStart(null);
        entry.setEnd(null);
        Assertions.assertEquals(0, entry.getMillisecondsSpan());
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
        Assertions.assertTrue(entry2.compareTo(entry1) > 0);

        entry2.setStart("12:00");
        Assertions.assertEquals(0, entry1.compareTo(entry2));
    }

    @Test
    void testGetDate() {
        ActivityLogEntry entry = new ActivityLogEntry();
        Date now = new Date();
        entry.setDate(now);
        Assertions.assertNotNull(entry.getDate());
        Assertions.assertEquals(now, entry.getDate());
    }

    @Test
    void testGetEquipment() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setEquipment(Collections.singletonList("Excavator"));
        List<String> equipment = entry.getEquipment();
        Assertions.assertNotNull(equipment);
        Assertions.assertEquals(1, equipment.size());
        Assertions.assertEquals("Excavator", equipment.get(0));
    }

    @Test
    void testGetOrganizationId() {
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setOrganizationId("org123");
        Assertions.assertEquals("org123", entry.getOrganizationId());
    }
}