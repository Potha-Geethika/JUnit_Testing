package com.carbo.activitylog.services;

import com.carbo.activitylog.model.Job;
import com.carbo.activitylog.model.Pad;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.ZoneId;
import java.util.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.TimeZone;
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






class DistrictFleetTimeZoneServiceTest {

    @Mock
    private PadService padService;

    @InjectMocks
    private DistrictFleetTimeZoneService districtFleetTimeZoneService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getZone_HappyPath_ReturnsZoneId() {
        Job job = new Job();
        job.setPad("pad-id");

        Pad pad = new Pad();
        pad.setTimezone("America/New_York");

        when(padService.getByName("org-id", "pad-id")).thenReturn(Optional.of(pad));

        ZoneId zoneId = districtFleetTimeZoneService.getZone("org-id", job);
        assertEquals(ZoneId.of("America/New_York"), zoneId);
    }

    @Test
    void getZone_NoPadFound_ReturnsNull() {
        Job job = new Job();
        job.setPad("non-existent-pad-id");

        when(padService.getByName("org-id", "non-existent-pad-id")).thenReturn(Optional.empty());

        ZoneId zoneId = districtFleetTimeZoneService.getZone("org-id", job);
        assertNull(zoneId);
    }

    @Test
    void getZone_PadWithoutTimezone_ReturnsNull() {
        Job job = new Job();
        job.setPad("pad-id");

        Pad pad = new Pad();
        pad.setTimezone(null);

        when(padService.getByName("org-id", "pad-id")).thenReturn(Optional.of(pad));

        ZoneId zoneId = districtFleetTimeZoneService.getZone("org-id", job);
        assertNull(zoneId);
    }

    @Test
    void getZone_EmptyOrganizationId_ReturnsNull() {
        Job job = new Job();
        job.setPad("pad-id");

        Pad pad = new Pad();
        pad.setTimezone("America/New_York");

        when(padService.getByName("", "pad-id")).thenReturn(Optional.of(pad));

        ZoneId zoneId = districtFleetTimeZoneService.getZone("", job);
        assertEquals(ZoneId.of("America/New_York"), zoneId);
    }
}