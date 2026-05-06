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
class PadServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PadService padService;

    @MockBean
    private MongoTemplate mongoTemplate;

    private Pad pad;

    @BeforeEach
    void setUp() {
        pad = new Pad();
        pad.setId("1");
        pad.setName("Test Pad");
        pad.setOperatorId("Operator1");
    }

    @Test
    void getPads_HappyPath() throws Exception {
        when(padService.getByOrganizationIdIn(any())).thenReturn(Arrays.asList(pad));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("organizationId", "Org1");
        request.addHeader("organizationType", "OPERATOR");

        mockMvc.perform(get("/v1/pads/")
                .requestAttr("request", request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pad.getId()))
                .andExpect(jsonPath("$[0].name").value(pad.getName()));

        verify(padService).getByOrganizationIdIn(any());
    }

    @Test
    void getPads_NoPadsFound() throws Exception {
        when(padService.getByOrganizationIdIn(any())).thenReturn(Collections.emptyList());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("organizationId", "Org1");
        request.addHeader("organizationType", "OPERATOR");

        mockMvc.perform(get("/v1/pads/")
                .requestAttr("request", request))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(padService).getByOrganizationIdIn(any());
    }

    @Test
    void getPad_HappyPath() throws Exception {
        when(padService.getPad("1")).thenReturn(Optional.of(pad));

        mockMvc.perform(get("/v1/pads/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(pad.getId()))
                .andExpect(jsonPath("name").value(pad.getName()));

        verify(padService).getPad("1");
    }

    @Test
    void getPad_NotFound() throws Exception {
        when(padService.getPad("1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/pads/1"))
                .andExpect(status().isInternalServerError());

        verify(padService).getPad("1");
    }

    @Test
    void updatePad_HappyPath() throws Exception {
        mockMvc.perform(put("/v1/pads/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"name\":\"Updated Pad\",\"operatorId\":\"Operator1\"}"))
                .andExpect(status().isOk());

        verify(padService).updatePad(any(Pad.class));
    }

    @Test
    void savePad_HappyPath() throws Exception {
        mockMvc.perform(post("/v1/pads/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Pad\",\"operatorId\":\"Operator1\"}"))
                .andExpect(status().isOk());

        verify(padService).savePad(any(Pad.class));
    }

    @Test
    void deletePad_HappyPath() throws Exception {
        mockMvc.perform(delete("/v1/pads/1"))
                .andExpect(status().isNoContent());

        verify(padService).deletePad("1");
    }

    @Test
    void updatePad_InternalServerError() throws Exception {
        doThrow(new RuntimeException()).when(padService).updatePad(any(Pad.class));

        mockMvc.perform(put("/v1/pads/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"name\":\"Updated Pad\",\"operatorId\":\"Operator1\"}"))
                .andExpect(status().isInternalServerError());
    }
}