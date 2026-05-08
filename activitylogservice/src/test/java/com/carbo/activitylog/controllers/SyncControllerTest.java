package com.carbo.activitylog.controllers;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.activitylog.model.ActivityLogEntry;
import com.carbo.activitylog.model.DeletedActivityLogEntry;
import com.carbo.activitylog.model.SyncRequest;
import com.carbo.activitylog.model.SyncResponse;
import com.carbo.activitylog.services.ActivityLogService;
import com.carbo.activitylog.services.DeletedActivityLogService;
import com.carbo.activitylog.utils.Constants;
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
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationId;
import static com.carbo.activitylog.utils.ControllerUtil.getUserFullName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;






@WebMvcTest(SyncController.class)
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private DeletedActivityLogService deletedActivityLogService;

    @InjectMocks
    private SyncController syncController;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
    }

    @Test
    void testView_HappyPath() throws Exception {
        String jobId = "job123";
        String organizationId = "org123";
        request.addHeader("OrganizationId", organizationId);

        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setId("entry123");
        entry.setTs(System.currentTimeMillis());

        when(activityLogService.findByOrganizationIdAndJobId(organizationId, jobId)).thenReturn(Collections.singletonList(entry));

        mockMvc.perform(get("/v1/sync/view")
                .param("jobId", jobId)
                .servletRequest(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entry123").exists())
                .andExpect(jsonPath("$.entry123").value(entry.getTs()));

        verify(activityLogService, times(1)).findByOrganizationIdAndJobId(organizationId, jobId);
    }

    @Test
    void testSync_HappyPath() throws Exception {
        String jobId = "job123";
        String organizationId = "org123";
        request.addHeader("OrganizationId", organizationId);

        SyncRequest<ActivityLogEntry> syncRequest = new SyncRequest<>();
        List<ActivityLogEntry> updates = new ArrayList<>();
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setId("entry123");
        updates.add(entry);
        syncRequest.setUpdate(updates);

        Map<String, Long> updatedMap = new HashMap<>();
        updatedMap.put(entry.getId(), System.currentTimeMillis());

        when(activityLogService.findByOrganizationIdAndJobId(organizationId, jobId)).thenReturn(Collections.emptyList());
        when(activityLogService.saveActivityLog(any(ActivityLogEntry.class))).thenReturn(entry);
        
        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"entry123\", \"ts\":123456}]}") // JSON representation of SyncRequest
                .servletRequest(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated").exists())
                .andExpect(jsonPath("$.updated.entry123").value(updatedMap.get(entry.getId())));

        verify(activityLogService, times(1)).saveActivityLog(any(ActivityLogEntry.class));
    }

    @Test
    void testSync_BadRequest_DuplicateKey() throws Exception {
        String jobId = "job123";
        String organizationId = "org123";
        request.addHeader("OrganizationId", organizationId);

        SyncRequest<ActivityLogEntry> syncRequest = new SyncRequest<>();
        List<ActivityLogEntry> updates = new ArrayList<>();
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setId("entry123");
        updates.add(entry);
        syncRequest.setUpdate(updates);

        when(activityLogService.findByOrganizationIdAndJobId(organizationId, jobId)).thenReturn(Collections.emptyList());
        when(activityLogService.saveActivityLog(any(ActivityLogEntry.class))).thenThrow(new DuplicateKeyException("Duplicate key"));

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"entry123\", \"ts\":123456}]}") // JSON representation of SyncRequest
                .servletRequest(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(Constants.DUPLICATE_RECORD_FOUND)));

        verify(activityLogService, times(1)).saveActivityLog(any(ActivityLogEntry.class));
    }

    @Test
    void testSync_InternalServerError() throws Exception {
        String jobId = "job123";
        String organizationId = "org123";
        request.addHeader("OrganizationId", organizationId);

        SyncRequest<ActivityLogEntry> syncRequest = new SyncRequest<>();
        List<ActivityLogEntry> updates = new ArrayList<>();
        ActivityLogEntry entry = new ActivityLogEntry();
        updates.add(entry);
        syncRequest.setUpdate(updates);

        when(activityLogService.findByOrganizationIdAndJobId(organizationId, jobId)).thenReturn(Collections.emptyList());
        when(activityLogService.saveActivityLog(any(ActivityLogEntry.class))).thenThrow(new RuntimeException("Internal server error"));

        mockMvc.perform(post("/v1/sync/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"update\":[{\"id\":\"entry123\", \"ts\":123456}]}") // JSON representation of SyncRequest
                .servletRequest(request))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString(Constants.ERROR_WHILE_CREATE_OR_UPDATE)));

        verify(activityLogService, times(1)).saveActivityLog(any(ActivityLogEntry.class));
    }
}