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
    void testResolveTimeZone_WithValidTimeZone() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Time-Zone")).thenReturn("America/New_York");
        
        ZoneId result = CommonUtils.resolveTimeZone(request);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals("America/New_York", result.getId());
    }

    @Test
    void testResolveTimeZone_WithInvalidTimeZone() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Time-Zone")).thenReturn("Invalid/Zone");
        
        ZoneId result = CommonUtils.resolveTimeZone(request);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals("UTC", result.getId());
    }

    @Test
    void testResolveTimeZone_WithNoTimeZoneHeader() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Time-Zone")).thenReturn(null);
        
        ZoneId result = CommonUtils.resolveTimeZone(request);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals("UTC", result.getId());
    }

    @Test
    void testRound() {
        Double number = 123.456789;
        Double result = CommonUtils.round(number, 2);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals(123.46, result);
    }

    @Test
    void testRound_WithNegativeDecimalPlaces() {
        Double number = 123.456789;
        Double result = CommonUtils.round(number, -1);
        
        Assertions.assertNotNull(result);
        Assertions.assertEquals(120.0, result);
    }

    @Test
    void testFormatMillisToHHmm() {
        String result = CommonUtils.formatMillisToHHmm(3661000);
        
        Assertions.assertEquals("01:01", result);
    }

    @Test
    void testFormatMillisToHHmm_WithZeroMillis() {
        String result = CommonUtils.formatMillisToHHmm(0);
        
        Assertions.assertEquals("00:00", result);
    }

    @Test
    void testFormatMillisToHHmm_WithNegativeMillis() {
        String result = CommonUtils.formatMillisToHHmm(-60000);
        
        Assertions.assertEquals("00:00", result);
    }
}