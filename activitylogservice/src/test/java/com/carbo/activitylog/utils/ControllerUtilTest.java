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

        String organizationId = ControllerUtil.getOrganizationId(request);
        Assertions.assertEquals("org123", organizationId);
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
        details.put("organizationType", "Non-Profit");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String organizationType = ControllerUtil.getOrganizationType(request);
        Assertions.assertEquals("Non-Profit", organizationType);
    }

    @Test
    void testGetOrganizationName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organizationName", "Carbo Inc.");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String organizationName = ControllerUtil.getOrganizationName(request);
        Assertions.assertEquals("Carbo Inc.", organizationName);
    }

    @Test
    void testGetUserName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("userName", "johndoe");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String userName = ControllerUtil.getUserName(request);
        Assertions.assertEquals("johndoe", userName);
    }

    @Test
    void testGetUserFullNameWithNullValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("fullName", null);
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String userFullName = ControllerUtil.getUserFullName(request);
        Assertions.assertNull(userFullName);
    }

    @Test
    void testGetOrganizationNameWithNullValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organizationName", null);
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String organizationName = ControllerUtil.getOrganizationName(request);
        Assertions.assertEquals("", organizationName);
    }

    @Test
    void testGetUserNameWithNullValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("userName", null);
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);

        String userName = ControllerUtil.getUserName(request);
        Assertions.assertEquals("", userName);
    }

    @Test
    void testGetOrganizationIdWithNonJwtPrincipal() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(Mockito.mock(Principal.class));

        Assertions.assertThrows(NullPointerException.class, () -> {
            ControllerUtil.getOrganizationId(request);
        });
    }

    @Test
    void testGetUserFullNameWithNonJwtPrincipal() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(Mockito.mock(Principal.class));

        Assertions.assertThrows(NullPointerException.class, () -> {
            ControllerUtil.getUserFullName(request);
        });
    }

    @Test
    void testGetOrganizationTypeWithNonJwtPrincipal() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(Mockito.mock(Principal.class));

        Assertions.assertThrows(ClassCastException.class, () -> {
            ControllerUtil.getOrganizationType(request);
        });
    }

    @Test
    void testGetOrganizationNameWithNonJwtPrincipal() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(Mockito.mock(Principal.class));

        Assertions.assertEquals("", ControllerUtil.getOrganizationName(request));
    }

    @Test
    void testGetUserNameWithNonJwtPrincipal() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(Mockito.mock(Principal.class));

        Assertions.assertEquals("", ControllerUtil.getUserName(request));
    }
}