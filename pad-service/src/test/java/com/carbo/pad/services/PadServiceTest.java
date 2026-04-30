package com.carbo.pad.services;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.pad.events.source.PadTimezoneSourceBean;
import com.carbo.pad.model.Pad;
import com.carbo.pad.repository.PadMongoDbRepository;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Mock
    private PadTimezoneSourceBean padTimezoneSourceBean;

    @InjectMocks
    private PadService padService;

    private Pad pad;

    @BeforeEach
    void setUp() {
        pad = new Pad();
        pad.setId("test-pad-id");
        pad.setName("Test Pad");
        pad.setTimezone("UTC");
        pad.setOperatorId("test-operator-id");
        pad.setOrganizationId("test-organization-id");
    }

    @Test
    void testGetAll() {
        when(padRepository.findAll()).thenReturn(Collections.singletonList(pad));
        List<Pad> result = padService.getAll();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(pad, result.get(0));
    }

    @Test
    void testGetByOrganizationId() {
        when(padRepository.findByOrganizationId("test-organization-id")).thenReturn(Collections.singletonList(pad));
        List<Pad> result = padService.getByOrganizationId("test-organization-id");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(pad, result.get(0));
    }

    @Test
    void testGetPad() {
        when(padRepository.findById("test-pad-id")).thenReturn(Optional.of(pad));
        Optional<Pad> result = padService.getPad("test-pad-id");
        assertNotNull(result);
        assertEquals(pad, result.get());
    }

    @Test
    void testSavePad() {
        when(padRepository.save(pad)).thenReturn(pad);
        Pad result = padService.savePad(pad);
        assertNotNull(result);
        assertEquals(pad, result);
    }

    @Test
    void testUpdatePad_TimezoneChanged() {
        Pad existingPad = new Pad();
        existingPad.setId("test-pad-id");
        existingPad.setTimezone("PST");
        when(padRepository.findById("test-pad-id")).thenReturn(Optional.of(existingPad));
        
        padService.updatePad(pad);
        
        verify(padTimezoneSourceBean, times(1)).publishPadTimezoneChange(eq("UPDATE"), eq(pad), eq("PST"));
        verify(padRepository, times(1)).save(pad);
    }

    @Test
    void testUpdatePad_TimezoneNotChanged() {
        Pad existingPad = new Pad();
        existingPad.setId("test-pad-id");
        existingPad.setTimezone("UTC");
        when(padRepository.findById("test-pad-id")).thenReturn(Optional.of(existingPad));
        
        padService.updatePad(pad);
        
        verify(padTimezoneSourceBean, times(0)).publishPadTimezoneChange(any(), any(), any());
        verify(padRepository, times(1)).save(pad);
    }

    @Test
    void testUpdatePad_NoExistingPad() {
        when(padRepository.findById("test-pad-id")).thenReturn(Optional.empty());
        
        padService.updatePad(pad);
        
        verify(padTimezoneSourceBean, times(0)).publishPadTimezoneChange(any(), any(), any());
        verify(padRepository, times(1)).save(pad);
    }

    @Test
    void testDeletePad() {
        doNothing().when(padRepository).deleteById("test-pad-id");
        padService.deletePad("test-pad-id");
        verify(padRepository, times(1)).deleteById("test-pad-id");
    }

    @Test
    void testGetByOrganizationIdIn() {
        when(padRepository.findByOrganizationIdIn(Set.of("org1", "org2"))).thenReturn(Collections.singletonList(pad));
        List<Pad> result = padService.getByOrganizationIdIn(Set.of("org1", "org2"));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(pad, result.get(0));
    }
}