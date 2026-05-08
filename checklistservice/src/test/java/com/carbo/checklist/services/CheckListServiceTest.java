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
        checkList.setLocked(false);
    }

    @Test
    void getAll_ShouldReturnListOfCheckLists() {
        when(checkListRepository.findAll()).thenReturn(Collections.singletonList(checkList));

        var result = checkListService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(checkList, result.get(0));
    }

    @Test
    void getByJobId_ShouldReturnListOfCheckLists() {
        when(checkListRepository.findByJobId("job1")).thenReturn(Collections.singletonList(checkList));

        var result = checkListService.getByJobId("job1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(checkList, result.get(0));
    }

    @Test
    void getCheckList_ShouldReturnCheckListWhenExists() {
        when(checkListRepository.findById("1")).thenReturn(Optional.of(checkList));

        var result = checkListService.getCheckList("1");

        assertNotNull(result);
        assertEquals(checkList, result.get());
    }

    @Test
    void getCheckList_ShouldReturnEmptyWhenNotExists() {
        when(checkListRepository.findById("2")).thenReturn(Optional.empty());

        var result = checkListService.getCheckList("2");

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void saveCheckList_ShouldReturnSavedCheckList() {
        when(checkListRepository.save(checkList)).thenReturn(checkList);

        var result = checkListService.saveCheckList(checkList);

        assertNotNull(result);
        assertEquals(checkList, result);
    }

    @Test
    void updateCheckList_ShouldSaveCheckList() {
        checkListService.updateCheckList(checkList);

        Mockito.verify(checkListRepository).save(checkList);
    }

    @Test
    void deleteCheckList_ShouldDeleteCheckList() {
        checkListService.deleteCheckList("1");

        Mockito.verify(checkListRepository).deleteById("1");
    }
}