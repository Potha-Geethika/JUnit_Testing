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
        details.put("organizationId", "org123");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String result = ControllerUtil.getOrganizationId(request);
        Assertions.assertEquals("org123", result);
    }

    @Test
    void testGetUserFullName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("fullName", "John Doe");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String result = ControllerUtil.getUserFullName(request);
        Assertions.assertEquals("John Doe", result);
    }

    @Test
    void testGetCurDay() {
        Long jobStartDate = 1680000000000L; // Example timestamp
        ZoneId zone = ZoneId.of("UTC");
        Integer result = ControllerUtil.getCurDay(jobStartDate, zone);
        Assertions.assertNotNull(result);
    }

    @Test
    void testGetOrganizationType() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organizationType", "Non-Profit");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String result = ControllerUtil.getOrganizationType(request);
        Assertions.assertEquals("Non-Profit", result);
    }

    @Test
    void testGetOrganizationName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organizationName", "Example Org");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String result = ControllerUtil.getOrganizationName(request);
        Assertions.assertEquals("Example Org", result);
    }

    @Test
    void testGetUserName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("userName", "jdoe");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String result = ControllerUtil.getUserName(request);
        Assertions.assertEquals("jdoe", result);
    }

    @Test
    void testGetUserFullNameWithNullField() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("fullName", null);
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String result = ControllerUtil.getUserFullName(request);
        Assertions.assertNull(result);
    }

    @Test
    void testGetUserNameWithNullField() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("userName", null);
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String result = ControllerUtil.getUserName(request);
        Assertions.assertEquals("", result);
    }

    @Test
    void testGetOrganizationNameWithNullField() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organizationName", null);
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String result = ControllerUtil.getOrganizationName(request);
        Assertions.assertEquals("", result);
    }
}