package com.carbo.pad.controllers;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.pad.model.JobDTO;
import com.carbo.pad.model.Pad;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.*;
import java.util.stream.Collectors;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import static com.carbo.pad.utils.Constants.OPERATOR;
import static com.carbo.pad.utils.Constants.SHARED_ORGANIZATION_ID;
import static com.carbo.pad.utils.ControllerUtil.getOrganizationId;
import static com.carbo.pad.utils.ControllerUtil.getOrganizationType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;







@WebMvcTest(PadServiceController.class)
public class PadServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PadService padService;

    @MockBean
    private MongoTemplate mongoTemplate;

    private HttpServletRequest request;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
        // Set up any request attributes here if needed
    }

    @Test
    public void testGetPads_HappyPath() throws Exception {
        Pad pad1 = new Pad();
        pad1.setId("1");
        pad1.setName("Pad 1");

        Pad pad2 = new Pad();
        pad2.setId("2");
        pad2.setName("Pad 2");

        when(padService.getByOrganizationIdIn(any())).thenReturn(Arrays.asList(pad1, pad2));

        mockMvc.perform(get("/v1/pads/")
                        .requestAttr("request", request))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Pad 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Pad 2"));

        verify(padService).getByOrganizationIdIn(any());
    }

    @Test
    public void testGetPad_HappyPath() throws Exception {
        Pad pad = new Pad();
        pad.setId("1");
        pad.setName("Pad 1");

        when(padService.getPad("1")).thenReturn(Optional.of(pad));

        mockMvc.perform(get("/v1/pads/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Pad 1"));

        verify(padService).getPad("1");
    }

    @Test
    public void testGetPad_InternalServerError() throws Exception {
        when(padService.getPad("1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/pads/1"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        verify(padService).getPad("1");
    }

    @Test
    public void testUpdatePad() throws Exception {
        Pad pad = new Pad();
        pad.setId("1");
        pad.setName("Updated Pad");

        mockMvc.perform(put("/v1/pads/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\",\"name\":\"Updated Pad\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(padService).updatePad(any(Pad.class));
    }

    @Test
    public void testSavePad() throws Exception {
        Pad pad = new Pad();
        pad.setId("1");
        pad.setName("New Pad");

        mockMvc.perform(post("/v1/pads/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\",\"name\":\"New Pad\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(padService).savePad(any(Pad.class));
    }

    @Test
    public void testDeletePad() throws Exception {
        mockMvc.perform(delete("/v1/pads/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        verify(padService).deletePad("1");
    }
}