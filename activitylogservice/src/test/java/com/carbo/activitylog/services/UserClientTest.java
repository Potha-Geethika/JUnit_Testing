package com.carbo.activitylog.services;
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

    @Value("${security.oauth2.resource.userInfoUri}")
    private String USER_API_URL = "http://localhost:8080/userinfo";

    @InjectMocks
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        // Set the USER_API_URL if necessary
    }

    @Test
    void testGetUserInfo_Success() {
        String accessToken = "test-access-token";
        Map<String, Object> expectedResponse = Map.of("key", "value");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);

        Map<String, Object> actualResponse = userClient.getUserInfo(accessToken);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testGetUserInfo_EmptyResponse() {
        String accessToken = "test-access-token";
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(Collections.emptyMap(), HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);

        Map<String, Object> actualResponse = userClient.getUserInfo(accessToken);

        assertEquals(Collections.emptyMap(), actualResponse);
    }

    @Test
    void testGetUserInfo_ErrorHandling() {
        String accessToken = "test-access-token";
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);

        Map<String, Object> actualResponse = userClient.getUserInfo(accessToken);

        assertEquals(null, actualResponse);
    }

    @Test
    void testGetUserInfo_NullAccessToken() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userClient.getUserInfo(null);
        });
        assertEquals("Access token cannot be null", exception.getMessage());
    }

    @Test
    void testGetUserInfo_EmptyAccessToken() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userClient.getUserInfo("");
        });
        assertEquals("Access token cannot be empty", exception.getMessage());
    }
}