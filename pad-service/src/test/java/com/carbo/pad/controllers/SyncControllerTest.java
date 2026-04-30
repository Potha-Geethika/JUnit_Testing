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

    private final HttpServletRequest request = new MockHttpServletRequest();

    @Test
    void view_happyPath() throws Exception {
        List<Pad> pads = Collections.singletonList(new Pad());
        pads.get(0).setId("padId");
        pads.get(0).setTs(123L);

        when(padService.getByOrganizationId(any())).thenReturn(pads);

        mockMvc.perform(get("/v1/sync/view")
                .requestAttr("request", request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.padId").value(123L));

        verify(padService).getByOrganizationId(any());
    }

    @Test
    void view_serviceThrowsException() throws Exception {
        when(padService.getByOrganizationId(any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/v1/sync/view")
                .requestAttr("request", request))
                .andExpect(status().isInternalServerError());

        verify(padService).getByOrganizationId(any());
    }

    @Test
    void sync_happyPath_insert() throws Exception {
        SyncRequest syncRequest = new SyncRequest();
        Pad pad = new Pad();
        pad.setId("padId");
        pad.setOrganizationId("orgId");
        pad.setTs(0L);
        syncRequest.setUpdate(Collections.singletonList(pad));

        when(padService.savePad(any())).thenReturn(pad);

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"padId\",\"organizationId\":\"orgId\",\"ts\":0}],\"remove\":[],\"get\":[]}"))
                .andExpect(status().isOk());

        verify(padService).savePad(any());
    }

    @Test
    void sync_update_invalidOrganizationId() throws Exception {
        SyncRequest syncRequest = new SyncRequest();
        Pad pad = new Pad();
        pad.setId("padId");
        pad.setOrganizationId("differentOrgId");
        pad.setTs(123L);
        syncRequest.setUpdate(Collections.singletonList(pad));

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"padId\",\"organizationId\":\"differentOrgId\",\"ts\":123}],\"remove\":[],\"get\":[]}"))
                .andExpect(status().isOk()); // Should just skip update, no error

        verify(padService, Mockito.never()).updatePad(any());
    }

    @Test
    void sync_serviceThrowsException() throws Exception {
        SyncRequest syncRequest = new SyncRequest();
        Pad pad = new Pad();
        pad.setId("padId");
        pad.setOrganizationId("orgId");
        pad.setTs(123L);
        syncRequest.setUpdate(Collections.singletonList(pad));

        when(padService.getPad(any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"padId\",\"organizationId\":\"orgId\",\"ts\":123}],\"remove\":[],\"get\":[]}"))
                .andExpect(status().isInternalServerError());

        verify(padService).getPad(any());
    }
}