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

    private SyncRequest syncRequest;

    @BeforeEach
    void setUp() {
        syncRequest = new SyncRequest();
    }

    @Test
    void view_shouldReturn200() throws Exception {
        String organizationId = "org123";
        Pad pad = new Pad();
        pad.setId("pad1");
        pad.setTs(System.currentTimeMillis());

        when(padService.getByOrganizationId(organizationId)).thenReturn(Collections.singletonList(pad));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("organizationId", organizationId);

        mockMvc.perform(get("/v1/sync/view")
                .requestAttr("organizationId", organizationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pad1").exists());

        verify(padService).getByOrganizationId(organizationId);
    }

    @Test
    void sync_shouldHandleHappyPath() throws Exception {
        String organizationId = "org123";
        Pad pad = new Pad();
        pad.setId("pad1");
        pad.setTs(1L);
        pad.setOrganizationId(organizationId);

        syncRequest.setUpdate(Collections.singletonList(pad));

        when(padService.getPad(pad.getId())).thenReturn(Optional.of(pad));
        when(padService.savePad(any(Pad.class))).thenReturn(pad);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("organizationId", organizationId);

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"pad1\",\"ts\":1,\"organizationId\":\"org123\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated.pad1").value(pad.getTs()));

        verify(padService).updatePad(pad);
    }

    @Test
    void sync_shouldHandleRemoval() throws Exception {
        String organizationId = "org123";
        String padIdToRemove = "pad1";

        syncRequest.setRemove(new HashSet<>(Collections.singletonList(padIdToRemove)));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("organizationId", organizationId);

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"remove\":[\"pad1\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.removed").isArray());

        verify(padService).deletePad(padIdToRemove);
    }

    @Test
    void sync_shouldReturn500WhenPadNotFound() throws Exception {
        Pad pad = new Pad();
        pad.setId("pad1");

        syncRequest.setUpdate(Collections.singletonList(pad));

        when(padService.getPad(pad.getId())).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("organizationId", "org123");

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"pad1\"}]}"))
                .andExpect(status().isInternalServerError());
    }
}