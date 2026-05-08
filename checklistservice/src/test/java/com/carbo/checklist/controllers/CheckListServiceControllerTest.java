package com.carbo.checklist.controllers;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.checklist.model.CheckList;
import com.carbo.checklist.services.CheckListService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.*;
import static com.carbo.checklist.utils.ControllerUtil.getOrganizationId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;







@WebMvcTest(CheckListServiceController.class)
public class CheckListServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckListService checkListService;

    private CheckList checkList;

    @BeforeEach
    public void setUp() {
        checkList = new CheckList();
        checkList.setId("1");
        checkList.setJobId("job1");
        checkList.setDay(1);
        checkList.setShift("morning");
    }

    @Test
    public void testGetCheckLists_HappyPath() throws Exception {
        when(checkListService.getByJobId("job1")).thenReturn(Collections.singletonList(checkList));

        mockMvc.perform(get("/v1/checklists/")
                .param("jobId", "job1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].jobId").value("job1"));
    }

    @Test
    public void testGetCheckLists_BadRequest() throws Exception {
        mockMvc.perform(get("/v1/checklists/"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetCheckList_HappyPath() throws Exception {
        when(checkListService.getCheckList("1")).thenReturn(Optional.of(checkList));

        mockMvc.perform(get("/v1/checklists/{checkListId}", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.jobId").value("job1"));
    }

    @Test
    public void testGetCheckList_NotFound() throws Exception {
        when(checkListService.getCheckList("1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/checklists/{checkListId}", "1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testUpdateCheckList_HappyPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Organization-Id", "org1");

        mockMvc.perform(put("/v1/checklists/{checkListId}", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobId\": \"job1\", \"day\": 1, \"shift\": \"morning\"}"))
                .andExpect(status().isOk());

        Mockito.verify(checkListService).updateCheckList(any(CheckList.class));
    }

    @Test
    public void testSaveCheckList_HappyPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Organization-Id", "org1");

        mockMvc.perform(post("/v1/checklists/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobId\": \"job1\", \"day\": 1, \"shift\": \"morning\"}"))
                .andExpect(status().isOk());

        Mockito.verify(checkListService).saveCheckList(any(CheckList.class));
    }

    @Test
    public void testDeleteCheckList_HappyPath() throws Exception {
        mockMvc.perform(delete("/v1/checklists/{checkListId}", "1"))
                .andExpect(status().isNoContent());

        Mockito.verify(checkListService).deleteCheckList("1");
    }

    @Test
    public void testGetCheckLists_InternalServerError() throws Exception {
        when(checkListService.getByJobId("job1")).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/v1/checklists/")
                .param("jobId", "job1"))
                .andExpect(status().isInternalServerError());
    }
}