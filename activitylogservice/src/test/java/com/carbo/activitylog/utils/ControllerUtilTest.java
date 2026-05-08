package com.carbo.activitylog.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
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





public class ControllerUtilTest {

    @Test
    void testGetOrganizationId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organizationId", "org-123");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String organizationId = ControllerUtil.getOrganizationId(request);
        Assertions.assertEquals("org-123", organizationId);
    }

    @Test
    void testGetUserFullName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("fullName", "John Doe");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String userFullName = ControllerUtil.getUserFullName(request);
        Assertions.assertEquals("John Doe", userFullName);
    }

    @Test
    void testGetOrganizationType() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organizationType", "typeA");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String organizationType = ControllerUtil.getOrganizationType(request);
        Assertions.assertEquals("typeA", organizationType);
    }

    @Test
    void testGetOrganizationName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organizationName", "Organization XYZ");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String organizationName = ControllerUtil.getOrganizationName(request);
        Assertions.assertEquals("Organization XYZ", organizationName);
    }

    @Test
    void testGetUserName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("userName", "testUser");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String userName = ControllerUtil.getUserName(request);
        Assertions.assertEquals("testUser", userName);
    }

    @Test
    void testGetCurDay() {
        // Test with a known date
        long jobStartDate = 1672531199000L; // Corresponds to 31st December 2022
        ZoneId zone = ZoneId.of("UTC");
        Integer curDay = ControllerUtil.getCurDay(jobStartDate, zone);
        Assertions.assertNotNull(curDay);
        Assertions.assertTrue(curDay > 0); // Ensure it returns a positive day count
    }
}