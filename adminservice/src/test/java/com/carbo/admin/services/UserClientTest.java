package com.carbo.admin.services;

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

    @Autowired
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userClient = new UserClient();
        userClient.restTemplate = restTemplate;
    }

    @Test
    void getUserInfo_HappyPath() {
        String accessToken = "test-token";
        Map<String, Object> expectedResponse = Map.of("name", "John Doe", "email", "john.doe@example.com");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        when(restTemplate.exchange(USER_API_URL, HttpMethod.GET, requestEntity, Map.class)).thenReturn(responseEntity);

        Map<String, Object> userInfo = userClient.getUserInfo(accessToken);
        assertNotNull(userInfo);
        assertEquals(expectedResponse, userInfo);
    }

    @Test
    void getUserInfo_EmptyResponse() {
        String accessToken = "test-token";
        Map<String, Object> expectedResponse = Collections.emptyMap();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        when(restTemplate.exchange(USER_API_URL, HttpMethod.GET, requestEntity, Map.class)).thenReturn(responseEntity);

        Map<String, Object> userInfo = userClient.getUserInfo(accessToken);
        assertNotNull(userInfo);
        assertEquals(expectedResponse, userInfo);
    }

    @Test
    void getUserInfo_ErrorHandling() {
        String accessToken = "test-token";
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        when(restTemplate.exchange(USER_API_URL, HttpMethod.GET, requestEntity, Map.class)).thenReturn(responseEntity);

        Map<String, Object> userInfo = userClient.getUserInfo(accessToken);
        assertNotNull(userInfo);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void getUserInfo_InvalidAccessToken() {
        String accessToken = null;
        Map<String, Object> expectedResponse = Collections.emptyMap();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.UNAUTHORIZED);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        when(restTemplate.exchange(USER_API_URL, HttpMethod.GET, requestEntity, Map.class)).thenReturn(responseEntity);

        Map<String, Object> userInfo = userClient.getUserInfo(accessToken);
        assertNotNull(userInfo);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }
}