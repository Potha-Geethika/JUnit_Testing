package com.carbo.checklist.services;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;

import com.carbo.checklist.model.CheckList;
import com.carbo.checklist.repository.CheckListMongoDbRepository;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
class CheckListServiceTest {

    @Mock
    private CheckListMongoDbRepository checkListRepository;

    @InjectMocks
    private CheckListService checkListService;

    private CheckList checkList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        checkList = new CheckList();
        checkList.setId("1");
        checkList.setJobId("job-123");
        checkList.setDay(1);
        checkList.setShift("morning");
        checkList.setLocked(false);
    }

    @Test
    void testGetAll() {
        when(checkListRepository.findAll()).thenReturn(Collections.singletonList(checkList));

        List<CheckList> result = checkListService.getAll();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(checkList, result.get(0));
    }

    @Test
    void testGetByJobId() {
        when(checkListRepository.findByJobId(anyString())).thenReturn(Collections.singletonList(checkList));

        List<CheckList> result = checkListService.getByJobId("job-123");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(checkList, result.get(0));
    }

    @Test
    void testGetCheckList() {
        when(checkListRepository.findById(anyString())).thenReturn(Optional.of(checkList));

        Optional<CheckList> result = checkListService.getCheckList("1");
        assertNotNull(result);
        assertEquals(checkList, result.get());
    }

    @Test
    void testSaveCheckList() {
        when(checkListRepository.save(any(CheckList.class))).thenReturn(checkList);

        CheckList result = checkListService.saveCheckList(checkList);
        assertNotNull(result);
        assertEquals(checkList, result);
    }

    @Test
    void testUpdateCheckList() {
        doNothing().when(checkListRepository).save(any(CheckList.class));

        checkListService.updateCheckList(checkList);
        verify(checkListRepository, times(1)).save(checkList);
    }

    @Test
    void testDeleteCheckList() {
        doNothing().when(checkListRepository).deleteById(anyString());

        checkListService.deleteCheckList("1");
        verify(checkListRepository, times(1)).deleteById("1");
    }
}