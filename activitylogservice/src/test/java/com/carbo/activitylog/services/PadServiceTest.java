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
        padService = new PadService(padRepository);
    }

    @Test
    void testGetAll_HappyPath() {
        List<Pad> expectedList = Collections.emptyList();
        when(padRepository.findAll()).thenReturn(expectedList);

        List<Pad> result = padService.getAll();

        assertNotNull(result);
        assertEquals(expectedList, result);
    }

    @Test
    void testGetByOrganizationId_HappyPath() {
        String organizationId = "org123";
        List<Pad> expectedList = Collections.emptyList();
        when(padRepository.findByOrganizationId(organizationId)).thenReturn(expectedList);

        List<Pad> result = padService.getByOrganizationId(organizationId);

        assertNotNull(result);
        assertEquals(expectedList, result);
    }

    @Test
    void testGetByName_HappyPath() {
        String organizationId = "org123";
        String name = "Pad1";
        Pad expectedPad = new Pad();
        expectedPad.setName(name);
        Optional<Pad> expectedOptional = Optional.of(expectedPad);
        when(padRepository.findDistinctByOrganizationIdAndName(organizationId, name)).thenReturn(expectedOptional);

        Optional<Pad> result = padService.getByName(organizationId, name);

        assertNotNull(result);
        assertEquals(expectedOptional, result);
    }

    @Test
    void testGetByName_EmptyResult() {
        String organizationId = "org123";
        String name = "Pad1";
        when(padRepository.findDistinctByOrganizationIdAndName(organizationId, name)).thenReturn(Optional.empty());

        Optional<Pad> result = padService.getByName(organizationId, name);

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testGetByOrganizationId_NonExistentId() {
        String organizationId = "nonExistentOrgId";
        List<Pad> expectedList = Collections.emptyList();
        when(padRepository.findByOrganizationId(organizationId)).thenReturn(expectedList);

        List<Pad> result = padService.getByOrganizationId(organizationId);

        assertNotNull(result);
        assertEquals(expectedList, result);
    }
}