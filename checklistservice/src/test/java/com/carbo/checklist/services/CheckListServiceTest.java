package com.carbo.checklist.services;

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
        checkList = new CheckList();
        checkList.setId("1");
        checkList.setJobId("job1");
        checkList.setDay(1);
        checkList.setShift("morning");
    }

    @Test
    void testGetAll() {
        List<CheckList> expectedList = Collections.singletonList(checkList);
        when(checkListRepository.findAll()).thenReturn(expectedList);

        List<CheckList> result = checkListService.getAll();

        assertEquals(expectedList, result);
        verify(checkListRepository, times(1)).findAll();
    }

    @Test
    void testGetByJobId() {
        List<CheckList> expectedList = Collections.singletonList(checkList);
        when(checkListRepository.findByJobId("job1")).thenReturn(expectedList);

        List<CheckList> result = checkListService.getByJobId("job1");

        assertEquals(expectedList, result);
        verify(checkListRepository, times(1)).findByJobId("job1");
    }

    @Test
    void testGetCheckList() {
        when(checkListRepository.findById("1")).thenReturn(Optional.of(checkList));

        Optional<CheckList> result = checkListService.getCheckList("1");

        assertEquals(Optional.of(checkList), result);
        verify(checkListRepository, times(1)).findById("1");
    }

    @Test
    void testSaveCheckList() {
        when(checkListRepository.save(checkList)).thenReturn(checkList);

        CheckList result = checkListService.saveCheckList(checkList);

        assertEquals(checkList, result);
        verify(checkListRepository, times(1)).save(checkList);
    }

    @Test
    void testUpdateCheckList() {
        checkListService.updateCheckList(checkList);

        verify(checkListRepository, times(1)).save(checkList);
    }

    @Test
    void testDeleteCheckList() {
        checkListService.deleteCheckList("1");

        verify(checkListRepository, times(1)).deleteById("1");
    }
}