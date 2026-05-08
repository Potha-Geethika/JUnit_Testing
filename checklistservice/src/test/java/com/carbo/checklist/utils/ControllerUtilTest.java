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





public class ControllerUtilTest {

    @Test
    void testGetOrganizationId() {
        String expectedOrganizationId = "org123";
        Map<String, Object> details = new HashMap<>();
        details.put("organizationId", expectedOrganizationId);

        JwtAuthenticationToken token = Mockito.mock(JwtAuthenticationToken.class);
        Mockito.doReturn(details).when(token).getDetails();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(token);

        String actualOrganizationId = ControllerUtil.getOrganizationId(request);
        Assertions.assertEquals(expectedOrganizationId, actualOrganizationId);
    }

    @Test
    void testGetOrganizationIdWithNullPrincipal() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(null);

        Assertions.assertThrows(ClassCastException.class, () -> {
            ControllerUtil.getOrganizationId(request);
        });
    }
}