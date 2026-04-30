package com.carbo.pad.utils;

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
    void testGetOrganization() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Map<String, Object> details = new HashMap<>();
        details.put("organization", "MyOrg");
        Mockito.doReturn(details).when(token).getDetails();
        request.setUserPrincipal(token);
        
        String organization = ControllerUtil.getOrganization(request);
        Assertions.assertEquals("MyOrg", organization);
    }
}