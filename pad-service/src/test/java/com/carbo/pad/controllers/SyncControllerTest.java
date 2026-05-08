package com.carbo.pad.controllers;
import static org.mockito.ArgumentMatchers.anyString;
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

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.addHeader("Organization-Id", "org123");
    }

    @Test
    void testView_HappyPath() throws Exception {
        Pad pad = new Pad();
        pad.setId("pad1");
        pad.setTs(123L);
        
        when(padService.getByOrganizationId(anyString())).thenReturn(Collections.singletonList(pad));

        mockMvc.perform(get("/v1/sync/view")
                .requestAttr("request", request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pad1").value(123L));

        verify(padService, times(1)).getByOrganizationId("org123");
    }

    @Test
    void testSync_HappyPath_UpdateAndInsert() throws Exception {
        SyncRequest syncRequest = new SyncRequest();
        Pad padToUpdate = new Pad();
        padToUpdate.setId("pad1");
        padToUpdate.setTs(100L);
        syncRequest.setUpdate(Collections.singletonList(padToUpdate));

        when(padService.getPad("pad1")).thenReturn(Optional.of(new Pad()));
        when(padService.savePad(any(Pad.class))).thenReturn(padToUpdate);

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"pad1\"}],\"remove\":[],\"get\":[]}")
                .requestAttr("request", request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated.pad1").value(100L));

        verify(padService, times(1)).updatePad(any(Pad.class));
        verify(padService, times(1)).savePad(any(Pad.class));
    }

    @Test
    void testSync_BadRequest_ValidationFailure() throws Exception {
        SyncRequest syncRequest = new SyncRequest();
        syncRequest.setUpdate(Collections.emptyList()); // Simulating validation failure by empty update list

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[],\"remove\":[],\"get\":[]}") // Invalid payload if validation was present
                .requestAttr("request", request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSync_InternalServerError() throws Exception {
        SyncRequest syncRequest = new SyncRequest();
        Pad padToUpdate = new Pad();
        padToUpdate.setId("pad1");
        padToUpdate.setTs(100L);
        syncRequest.setUpdate(Collections.singletonList(padToUpdate));

        when(padService.getPad("pad1")).thenReturn(Optional.empty()); // Simulating internal error case

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"pad1\"}],\"remove\":[],\"get\":[]}")
                .requestAttr("request", request))
                .andExpect(status().isInternalServerError());

        verify(padService, times(1)).getPad("pad1");
    }
}