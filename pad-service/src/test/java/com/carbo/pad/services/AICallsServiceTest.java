package com.carbo.pad.services;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.pad.model.*;
import com.google.gson.Gson;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.*;
import java.util.stream.Collectors;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;







@ExtendWith(MockitoExtension.class)
class AICallsServiceTest {

    @Mock
    private Client client;

    @Mock
    private WebTarget webTarget;

    @Mock
    private Response response;

    @Mock
    private ExecutorService executorService;

    @InjectMocks
    private AICallsService aiCallsService;

    private String wellsDirectApi = "http://localhost:8080/api/wells/";
    private String protocol = "http";
    private String server = "localhost";
    private int port = 8080;
    private String userName = "testUser";
    private String password = "testPass";




    @Test
    void testGetAllFracproTreatmentsDirect_treatmentIdsNull() {
        Map<Integer, FracProTreatment> result = aiCallsService.getAllFracproTreatmentsDirect(1, null, "test-token");
        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
    }



    @Test
    void testGetFracProTreatmentDirect_error() {
        when(response.getStatus()).thenReturn(500);
        aiCallsService.getFracProTreatmentDirect(1, 1, "test-token", true);
        assertEquals(null, aiCallsService.getFracProTreatmentDirect(1, 1, "test-token", true));
    }
}