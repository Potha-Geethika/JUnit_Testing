package com.carbo.checklist.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.Map;
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





class ControllerUtilTest {

    @Test
    void getOrganizationIdReturnsCorrectId() {
        Map<String, Object> details = new HashMap<>();
        details.put("organizationId", "org123");

        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Mockito.doReturn(details).when(token).getDetails();

        Principal principal = token;
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn(principal).when(request).getUserPrincipal();

        String organizationId = ControllerUtil.getOrganizationId(request);

        Assertions.assertEquals("org123", organizationId);
    }

    @Test
    void getOrganizationIdReturnsNullWhenPrincipalIsNull() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn(null).when(request).getUserPrincipal();

        String organizationId = ControllerUtil.getOrganizationId(request);

        Assertions.assertNull(organizationId);
    }

    @Test
    void getOrganizationIdThrowsExceptionWhenDetailsAreNotMap() {
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Mockito.doReturn("not_a_map").when(token).getDetails();

        Principal principal = token;
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn(principal).when(request).getUserPrincipal();

        Assertions.assertThrows(ClassCastException.class, () -> {
            ControllerUtil.getOrganizationId(request);
        });
    }
}