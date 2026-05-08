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
        pad.setName("Pad Name");
        pad.setOrganizationId("Org1");
        pad.setOperatorId("Op1");
    }

    @Test
    void testGetAll_HappyPath() {
        when(padRepository.findAll()).thenReturn(Collections.singletonList(pad));

        List<Pad> pads = padService.getAll();

        assertNotNull(pads);
        assertEquals(1, pads.size());
        assertEquals(pad.getId(), pads.get(0).getId());
    }

    @Test
    void testGetAll_EmptyList() {
        when(padRepository.findAll()).thenReturn(Collections.emptyList());

        List<Pad> pads = padService.getAll();

        assertNotNull(pads);
        assertEquals(0, pads.size());
    }

    @Test
    void testGetByOrganizationId_HappyPath() {
        when(padRepository.findByOrganizationId("Org1")).thenReturn(Collections.singletonList(pad));

        List<Pad> pads = padService.getByOrganizationId("Org1");

        assertNotNull(pads);
        assertEquals(1, pads.size());
        assertEquals(pad.getId(), pads.get(0).getId());
    }

    @Test
    void testGetByOrganizationId_EmptyList() {
        when(padRepository.findByOrganizationId("Org2")).thenReturn(Collections.emptyList());

        List<Pad> pads = padService.getByOrganizationId("Org2");

        assertNotNull(pads);
        assertEquals(0, pads.size());
    }

    @Test
    void testGetByName_HappyPath() {
        when(padRepository.findDistinctByOrganizationIdAndName("Org1", "Pad Name")).thenReturn(Optional.of(pad));

        Optional<Pad> foundPad = padService.getByName("Org1", "Pad Name");

        assertNotNull(foundPad);
        assertEquals(pad.getId(), foundPad.get().getId());
    }

    @Test
    void testGetByName_NotFound() {
        when(padRepository.findDistinctByOrganizationIdAndName("Org1", "Nonexistent Pad")).thenReturn(Optional.empty());

        Optional<Pad> foundPad = padService.getByName("Org1", "Nonexistent Pad");

        assertNotNull(foundPad);
        assertEquals(Optional.empty(), foundPad);
    }
}