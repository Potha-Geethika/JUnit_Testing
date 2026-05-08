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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        Pad pad = new Pad();
        when(padRepository.findAll()).thenReturn(Collections.singletonList(pad));

        List<Pad> pads = padService.getAll();
        assertNotNull(pads);
        assertEquals(1, pads.size());
    }

    @Test
    void testGetByOrganizationId() {
        Pad pad = new Pad();
        String organizationId = "org1";
        when(padRepository.findByOrganizationId(organizationId)).thenReturn(Collections.singletonList(pad));

        List<Pad> pads = padService.getByOrganizationId(organizationId);
        assertNotNull(pads);
        assertEquals(1, pads.size());
    }

    @Test
    void testGetPad() {
        Pad pad = new Pad();
        String padId = "pad1";
        when(padRepository.findById(padId)).thenReturn(Optional.of(pad));

        Optional<Pad> retrievedPad = padService.getPad(padId);
        assertTrue(retrievedPad.isPresent());
        assertEquals(pad, retrievedPad.get());
    }

    @Test
    void testSavePad() {
        Pad pad = new Pad();
        when(padRepository.save(pad)).thenReturn(pad);

        Pad savedPad = padService.savePad(pad);
        assertNotNull(savedPad);
        assertEquals(pad, savedPad);
    }

    @Test
    void testUpdatePad_timeZoneChanged() {
        Pad pad = new Pad();
        pad.setId("pad1");
        pad.setTimezone("GMT");
        
        Pad existingPad = new Pad();
        existingPad.setId("pad1");
        existingPad.setTimezone("UTC");

        when(padRepository.findById(pad.getId())).thenReturn(Optional.of(existingPad));
        doNothing().when(padTimezoneSourceBean).publishPadTimezoneChange(any(), any(), any());
        when(padRepository.save(pad)).thenReturn(pad);

        padService.updatePad(pad);

        verify(padTimezoneSourceBean, times(1)).publishPadTimezoneChange("UPDATE", pad, "UTC");
        verify(padRepository, times(1)).save(pad);
    }

    @Test
    void testUpdatePad_timeZoneNotChanged() {
        Pad pad = new Pad();
        pad.setId("pad1");
        pad.setTimezone("UTC");

        Pad existingPad = new Pad();
        existingPad.setId("pad1");
        existingPad.setTimezone("UTC");

        when(padRepository.findById(pad.getId())).thenReturn(Optional.of(existingPad));
        when(padRepository.save(pad)).thenReturn(pad);

        padService.updatePad(pad);

        verify(padTimezoneSourceBean, never()).publishPadTimezoneChange(any(), any(), any());
        verify(padRepository, times(1)).save(pad);
    }

    @Test
    void testUpdatePad_padNotFound() {
        Pad pad = new Pad();
        pad.setId("pad1");
        pad.setTimezone("GMT");

        when(padRepository.findById(pad.getId())).thenReturn(Optional.empty());
        when(padRepository.save(pad)).thenReturn(pad);

        padService.updatePad(pad);

        verify(padTimezoneSourceBean, never()).publishPadTimezoneChange(any(), any(), any());
        verify(padRepository, times(1)).save(pad);
    }

    @Test
    void testDeletePad() {
        String padId = "pad1";
        doNothing().when(padRepository).deleteById(padId);

        padService.deletePad(padId);

        verify(padRepository, times(1)).deleteById(padId);
    }

    @Test
    void testGetByOrganizationIdIn() {
        Pad pad = new Pad();
        Set<String> organizationIds = Set.of("org1", "org2");
        when(padRepository.findByOrganizationIdIn(organizationIds)).thenReturn(Collections.singletonList(pad));

        List<Pad> pads = padService.getByOrganizationIdIn(organizationIds);
        assertNotNull(pads);
        assertEquals(1, pads.size());
    }
}