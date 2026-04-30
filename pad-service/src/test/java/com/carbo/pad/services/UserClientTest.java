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

    @Mock
    private ResponseEntity<Map> responseEntity;

    @InjectMocks
    private UserClient userClient;

    @Value("${security.oauth2.resource.userInfoUri}")
    private String USER_API_URL = "http://localhost:8080/userinfo"; // Mocked value for testing

    @BeforeEach
    void setUp() {
        // Mocking the response entity behavior
        when(restTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)))
                .thenReturn(responseEntity);
    }

    @Test
    void getUserInfo_HappyPath() {
        String accessToken = "test-token";
        Map<String, Object> expectedResponse = Map.of("key", "value");

        when(responseEntity.getBody()).thenReturn(expectedResponse);

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        Assertions.assertEquals(expectedResponse, result);
    }

    @Test
    void getUserInfo_NullAccessToken() {
        String accessToken = null;
        when(responseEntity.getBody()).thenReturn(Collections.emptyMap());

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        Assertions.assertEquals(Collections.emptyMap(), result);
    }

    @Test
    void getUserInfo_EmptyAccessToken() {
        String accessToken = "";
        when(responseEntity.getBody()).thenReturn(Collections.emptyMap());

        Map<String, Object> result = userClient.getUserInfo(accessToken);

        Assertions.assertEquals(Collections.emptyMap(), result);
    }

    @Test
    void getUserInfo_ErrorHandling() {
        String accessToken = "test-token";

        when(restTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)))
                .thenThrow(new RuntimeException("Service Unavailable"));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            userClient.getUserInfo(accessToken);
        });
        Assertions.assertEquals("Service Unavailable", thrown.getMessage());
    }
}