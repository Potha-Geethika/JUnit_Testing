package com.carbo.activitylog.services;

import com.carbo.activitylog.repository.OperatorMongoDbRepository;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
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
class OperatorServiceTest {

    @Mock
    private OperatorMongoDbRepository operatorRepository;

    private OperatorService operatorService;

    @BeforeEach
    void setUp() {
        operatorService = new OperatorService(operatorRepository);
    }

    @Test
    void isShared_HappyPath_ReturnsTrue() {
        String sharedFromId = "org1";
        String sharedToId = "org2";
        when(operatorRepository.findByOrganizationIdAndLinkedOrganizationId(sharedFromId, sharedToId))
                .thenReturn(List.of(new com.carbo.activitylog.model.Operator()));

        Boolean result = operatorService.isShared(sharedFromId, sharedToId);

        assertEquals(true, result);
    }

    @Test
    void isShared_NoOperators_ReturnsFalse() {
        String sharedFromId = "org1";
        String sharedToId = "org2";
        when(operatorRepository.findByOrganizationIdAndLinkedOrganizationId(sharedFromId, sharedToId))
                .thenReturn(Collections.emptyList());

        Boolean result = operatorService.isShared(sharedFromId, sharedToId);

        assertEquals(false, result);
    }

    @Test
    void isShared_NullIds_ReturnsFalse() {
        Boolean result = operatorService.isShared(null, null);

        assertEquals(false, result);
    }

    @Test
    void isShared_EmptyIds_ReturnsFalse() {
        Boolean result = operatorService.isShared("", "");

        assertEquals(false, result);
    }
}