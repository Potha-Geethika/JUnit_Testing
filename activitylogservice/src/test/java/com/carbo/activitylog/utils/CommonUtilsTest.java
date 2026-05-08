package com.carbo.activitylog.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.ZoneId;
import java.util.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
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
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;





public class CommonUtilsTest {

    @Test
    void testResolveTimeZoneWithValidHeader() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Time-Zone")).thenReturn("America/New_York");

        ZoneId result = CommonUtils.resolveTimeZone(request);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("America/New_York", result.getId());
    }

    @Test
    void testResolveTimeZoneWithInvalidHeader() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Time-Zone")).thenReturn("Invalid/Zone");

        ZoneId result = CommonUtils.resolveTimeZone(request);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("UTC", result.getId());
    }

    @Test
    void testResolveTimeZoneWithNoHeader() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Time-Zone")).thenReturn(null);

        ZoneId result = CommonUtils.resolveTimeZone(request);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("UTC", result.getId());
    }

    @Test
    void testRound() {
        Double result = CommonUtils.round(2.34567, 2);
        Assertions.assertEquals(2.35, result);

        result = CommonUtils.round(2.34444, 2);
        Assertions.assertEquals(2.34, result);

        result = CommonUtils.round(2.0, 2);
        Assertions.assertEquals(2.00, result);
    }

    @Test
    void testFormatMillisToHHmm() {
        String result = CommonUtils.formatMillisToHHmm(3600000);
        Assertions.assertEquals("01:00", result);

        result = CommonUtils.formatMillisToHHmm(7200000);
        Assertions.assertEquals("02:00", result);

        result = CommonUtils.formatMillisToHHmm(0);
        Assertions.assertEquals("00:00", result);

        result = CommonUtils.formatMillisToHHmm(61000);
        Assertions.assertEquals("00:01", result);
    }
}