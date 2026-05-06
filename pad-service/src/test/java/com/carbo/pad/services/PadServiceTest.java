package com.carbo.pad.services;
import static org.mockito.ArgumentMatchers.anyString;
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

    private PadService padService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        padService = new PadService(padRepository, padTimezoneSourceBean);
    }

    @Test
    void testGetAll() {
        when(padRepository.findAll()).thenReturn(Collections.emptyList());
        List<Pad> pads = padService.getAll();
        assertNotNull(pads);
        assertEquals(0, pads.size());
    }

    @Test
    void testGetByOrganizationId() {
        String organizationId = "org-1";
        when(padRepository.findByOrganizationId(organizationId)).thenReturn(Collections.emptyList());
        List<Pad> pads = padService.getByOrganizationId(organizationId);
        assertNotNull(pads);
        assertEquals(0, pads.size());
    }

    @Test
    void testGetPad() {
        String padId = "pad-1";
        Pad pad = new Pad();
        pad.setId(padId);
        when(padRepository.findById(padId)).thenReturn(Optional.of(pad));
        Optional<Pad> foundPad = padService.getPad(padId);
        assertTrue(foundPad.isPresent());
        assertEquals(padId, foundPad.get().getId());
    }

    @Test
    void testSavePad() {
        Pad pad = new Pad();
        pad.setId("pad-1");
        when(padRepository.save(pad)).thenReturn(pad);
        Pad savedPad = padService.savePad(pad);
        assertNotNull(savedPad);
        assertEquals(pad.getId(), savedPad.getId());
    }

    @Test
    void testUpdatePad_TimeZoneChanged() {
        Pad pad = new Pad();
        pad.setId("pad-1");
        pad.setTimezone("UTC");
        Optional<Pad> existingPad = Optional.of(new Pad());
        existingPad.get().setTimezone("PST");
        when(padRepository.findById(pad.getId())).thenReturn(existingPad);
        
        padService.updatePad(pad);
        
        verify(padTimezoneSourceBean).publishPadTimezoneChange(eq("UPDATE"), eq(pad), eq("PST"));
        verify(padRepository).save(pad);
    }

    @Test
    void testUpdatePad_TimeZoneNotChanged() {
        Pad pad = new Pad();
        pad.setId("pad-1");
        pad.setTimezone("UTC");
        Optional<Pad> existingPad = Optional.of(new Pad());
        existingPad.get().setTimezone("UTC");
        when(padRepository.findById(pad.getId())).thenReturn(existingPad);
        
        padService.updatePad(pad);
        
        verify(padTimezoneSourceBean, never()).publishPadTimezoneChange(anyString(), any(), anyString());
        verify(padRepository).save(pad);
    }

    @Test
    void testDeletePad() {
        String padId = "pad-1";
        doNothing().when(padRepository).deleteById(padId);
        padService.deletePad(padId);
        verify(padRepository).deleteById(padId);
    }

    @Test
    void testGetByOrganizationIdIn() {
        Set<String> organizationIds = Set.of("org-1", "org-2");
        when(padRepository.findByOrganizationIdIn(organizationIds)).thenReturn(Collections.emptyList());
        List<Pad> pads = padService.getByOrganizationIdIn(organizationIds);
        assertNotNull(pads);
        assertEquals(0, pads.size());
    }
}