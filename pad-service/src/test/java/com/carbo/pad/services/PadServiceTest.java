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
        pad.setId("1");
        pad.setName("Test Pad");
        pad.setOrganizationId("Org1");
        pad.setTimezone("GMT");
    }

    @Test
    void testGetAll() {
        when(padRepository.findAll()).thenReturn(Collections.singletonList(pad));
        
        List<Pad> pads = padService.getAll();
        
        assertNotNull(pads);
        assertEquals(1, pads.size());
        assertEquals(pad, pads.get(0));
    }

    @Test
    void testGetByOrganizationId() {
        when(padRepository.findByOrganizationId("Org1")).thenReturn(Collections.singletonList(pad));
        
        List<Pad> pads = padService.getByOrganizationId("Org1");
        
        assertNotNull(pads);
        assertEquals(1, pads.size());
        assertEquals(pad, pads.get(0));
    }

    @Test
    void testGetPad() {
        when(padRepository.findById("1")).thenReturn(Optional.of(pad));
        
        Optional<Pad> foundPad = padService.getPad("1");
        
        assertTrue(foundPad.isPresent());
        assertEquals(pad, foundPad.get());
    }

    @Test
    void testSavePad() {
        when(padRepository.save(pad)).thenReturn(pad);
        
        Pad savedPad = padService.savePad(pad);
        
        assertNotNull(savedPad);
        assertEquals(pad, savedPad);
    }

    @Test
    void testUpdatePad_TimezoneChanged() {
        Pad existingPad = new Pad();
        existingPad.setId("1");
        existingPad.setTimezone("UTC");
        
        when(padRepository.findById("1")).thenReturn(Optional.of(existingPad));
        when(padRepository.save(pad)).thenReturn(pad);
        
        padService.updatePad(pad);
        
        verify(padTimezoneSourceBean).publishPadTimezoneChange(eq("UPDATE"), eq(pad), eq("UTC"));
        verify(padRepository).save(pad);
    }

    @Test
    void testUpdatePad_TimezoneNotChanged() {
        Pad existingPad = new Pad();
        existingPad.setId("1");
        existingPad.setTimezone("GMT");
        
        when(padRepository.findById("1")).thenReturn(Optional.of(existingPad));
        when(padRepository.save(pad)).thenReturn(pad);
        
        padService.updatePad(pad);
        
        verify(padTimezoneSourceBean, never()).publishPadTimezoneChange(any(), any(), any());
        verify(padRepository).save(pad);
    }

    @Test
    void testUpdatePad_PadNotFound() {
        when(padRepository.findById("1")).thenReturn(Optional.empty());
        when(padRepository.save(pad)).thenReturn(pad);
        
        padService.updatePad(pad);
        
        verify(padTimezoneSourceBean, never()).publishPadTimezoneChange(any(), any(), any());
        verify(padRepository).save(pad);
    }

    @Test
    void testDeletePad() {
        doNothing().when(padRepository).deleteById("1");
        
        padService.deletePad("1");
        
        verify(padRepository).deleteById("1");
    }

    @Test
    void testGetByOrganizationIdIn() {
        when(padRepository.findByOrganizationIdIn(Set.of("Org1"))).thenReturn(Collections.singletonList(pad));
        
        List<Pad> pads = padService.getByOrganizationIdIn(Set.of("Org1"));
        
        assertNotNull(pads);
        assertEquals(1, pads.size());
        assertEquals(pad, pads.get(0));
    }
}