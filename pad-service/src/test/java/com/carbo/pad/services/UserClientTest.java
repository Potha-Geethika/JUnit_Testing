package com.carbo.pad.services;
import static org.mockito.ArgumentMatchers.any;

import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;






@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserClient userClient;

    private String userApiUrl = "http://localhost:8080/userinfo";

    @BeforeEach
    void setUp() {
        // Assuming there's a way to set the private USER_API_URL, perhaps via a constructor or method.
        // If not, we will mock the behavior in the actual test where it's needed.
    }

    @Test
    void testGetUserInfo_Success() {
        String accessToken = "test-access-token";
        Map<String, Object> expectedResponse = Collections.singletonMap("key", "value");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> responseEntity = ResponseEntity.ok(expectedResponse);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void testGetUserInfo_NullAccessToken() {
        String accessToken = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> responseEntity = ResponseEntity.ok(Collections.singletonMap("key", "value"));
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        assertNotNull(result);
        assertEquals(Collections.singletonMap("key", "value"), result);
    }

    @Test
    void testGetUserInfo_EmptyResponse() {
        String accessToken = "test-access-token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> responseEntity = ResponseEntity.ok(Collections.emptyMap());
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenReturn(responseEntity);

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
    }

    @Test
    void testGetUserInfo_ErrorHandling() {
        String accessToken = "test-access-token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenThrow(new RuntimeException("API Error"));

        RuntimeException exception = null;
        try {
            userClient.getUserInfo(accessToken);
        } catch (RuntimeException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("API Error", exception.getMessage());
    }
}