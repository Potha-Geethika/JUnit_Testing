package com.carbo.pad.controllers;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.pad.model.Pad;
import com.carbo.pad.model.SyncRequest;
import com.carbo.pad.model.SyncResponse;
import com.carbo.pad.services.PadService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import static com.carbo.pad.utils.ControllerUtil.getOrganizationId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;






@WebMvcTest(SyncController.class)
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PadService padService;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        Mockito.when(request.getHeader("Organization-Id")).thenReturn("org123");
    }

    @Test
    void sync_ShouldReturnUpdatedResponseWithRemovedItems() throws Exception {
        SyncRequest syncRequest = new SyncRequest();
        syncRequest.setRemove(new HashSet<>(Arrays.asList("pad1", "pad2")));

        SyncResponse expectedResponse = new SyncResponse();
        expectedResponse.setRemoved(new HashSet<>(Arrays.asList("pad1", "pad2")));

        mockMvc.perform(post("/v1/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"remove\":[\"pad1\", \"pad2\"]}"))
                .andExpect(status().isOk());

        verify(padService, Mockito.times(1)).deletePad("pad1");
        verify(padService, Mockito.times(1)).deletePad("pad2");
    }

    @Test
    void sync_ShouldReturnUpdatedResponseWithNewPads() throws Exception {
        Pad newPad = new Pad();
        newPad.setId("pad3");
        newPad.setOrganizationId("org123");
        newPad.setTs(System.currentTimeMillis());

        SyncRequest syncRequest = new SyncRequest();
        syncRequest.setUpdate(Collections.singletonList(newPad));

        Mockito.when(padService.savePad(any(Pad.class))).thenReturn(newPad);

        mockMvc.perform(post("/v1/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"pad3\", \"organizationId\":\"org123\", \"ts\":12345}]}"))
                .andExpect(status().isOk());

        verify(padService, Mockito.times(1)).savePad(any(Pad.class));
    }

    @Test
    void sync_ShouldReturnUpdatedResponseWithExistingPads() throws Exception {
        Pad existingPad = new Pad();
        existingPad.setId("pad4");
        existingPad.setOrganizationId("org123");
        existingPad.setTs(12345L);

        SyncRequest syncRequest = new SyncRequest();
        syncRequest.setUpdate(Collections.singletonList(existingPad));

        Mockito.when(padService.getPad("pad4")).thenReturn(Optional.of(existingPad));

        mockMvc.perform(post("/v1/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"pad4\", \"organizationId\":\"org123\", \"ts\":12345}]}"))
                .andExpect(status().isOk());

        verify(padService, Mockito.times(1)).getPad("pad4");
    }

    @Test
    void sync_ShouldReturnInternalServerErrorWhenExceptionOccurs() throws Exception {
        SyncRequest syncRequest = new SyncRequest();
        syncRequest.setRemove(new HashSet<>(Arrays.asList("pad1")));

        Mockito.doThrow(new RuntimeException("Internal Server Error")).when(padService).deletePad("pad1");

        mockMvc.perform(post("/v1/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"remove\":[\"pad1\"]}"))
                .andExpect(status().isInternalServerError());
    }
}