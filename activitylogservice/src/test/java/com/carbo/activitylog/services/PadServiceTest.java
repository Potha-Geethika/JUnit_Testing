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

    private PadService padService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        padService = new PadService(padRepository);
    }

    @Test
    void testGetAll() {
        List<Pad> expectedPads = Collections.emptyList();
        doReturn(expectedPads).when(padRepository).findAll();

        List<Pad> result = padService.getAll();

        assertNotNull(result);
        assertEquals(expectedPads, result);
    }

    @Test
    void testGetByOrganizationId() {
        String organizationId = "org-001";
        List<Pad> expectedPads = Collections.singletonList(new Pad());
        doReturn(expectedPads).when(padRepository).findByOrganizationId(organizationId);

        List<Pad> result = padService.getByOrganizationId(organizationId);

        assertNotNull(result);
        assertEquals(expectedPads, result);
    }

    @Test
    void testGetByName() {
        String organizationId = "org-001";
        String name = "pad-name";
        Pad expectedPad = new Pad();
        doReturn(Optional.of(expectedPad)).when(padRepository).findDistinctByOrganizationIdAndName(organizationId, name);

        Optional<Pad> result = padService.getByName(organizationId, name);

        assertNotNull(result);
        assertEquals(expectedPad, result.get());
    }

    @Test
    void testGetByName_NotFound() {
        String organizationId = "org-001";
        String name = "non-existent-pad";
        doReturn(Optional.empty()).when(padRepository).findDistinctByOrganizationIdAndName(organizationId, name);

        Optional<Pad> result = padService.getByName(organizationId, name);

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }
}