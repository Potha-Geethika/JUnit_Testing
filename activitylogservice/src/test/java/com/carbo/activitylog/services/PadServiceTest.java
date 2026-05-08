package com.carbo.activitylog.services;

import com.carbo.activitylog.model.Pad;
import com.carbo.activitylog.repository.PadMongoDbRepository;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
class PadServiceTest {

    @Mock
    private PadMongoDbRepository padRepository;

    @InjectMocks
    private PadService padService;

    private Pad pad;

    @BeforeEach
    void setUp() {
        pad = new Pad();
        pad.setId("1");
        pad.setName("Test Pad");
        pad.setOperatorId("operator1");
        pad.setOrganizationId("org1");
    }

    @Test
    void testGetAll() {
        List<Pad> expectedList = Collections.singletonList(pad);
        doReturn(expectedList).when(padRepository).findAll();

        List<Pad> result = padService.getAll();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedList.get(0).getId(), result.get(0).getId());
    }

    @Test
    void testGetByOrganizationId() {
        List<Pad> expectedList = Collections.singletonList(pad);
        doReturn(expectedList).when(padRepository).findByOrganizationId("org1");

        List<Pad> result = padService.getByOrganizationId("org1");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedList.get(0).getId(), result.get(0).getId());
    }

    @Test
    void testGetByName() {
        doReturn(Optional.of(pad)).when(padRepository).findDistinctByOrganizationIdAndName("org1", "Test Pad");

        Optional<Pad> result = padService.getByName("org1", "Test Pad");
        assertNotNull(result);
        assertEquals(pad.getId(), result.get().getId());
    }

    @Test
    void testGetByName_NotFound() {
        doReturn(Optional.empty()).when(padRepository).findDistinctByOrganizationIdAndName("org1", "Nonexistent Pad");

        Optional<Pad> result = padService.getByName("org1", "Nonexistent Pad");
        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }
}