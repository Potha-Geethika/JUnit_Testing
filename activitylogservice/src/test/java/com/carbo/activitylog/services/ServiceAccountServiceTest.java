package com.carbo.activitylog.services;

import com.carbo.activitylog.model.ServiceAccount;
import com.carbo.activitylog.repository.ServiceAccountMongoDbRepository;
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
class ServiceAccountServiceTest {

    @Mock
    private ServiceAccountMongoDbRepository serviceAccountMongoDbRepository;

    @InjectMocks
    private ServiceAccountService serviceAccountService;

    private ServiceAccount serviceAccount;

    @BeforeEach
    void setUp() {
        serviceAccount = new ServiceAccount();
        serviceAccount.setId("1");
        serviceAccount.setOrganizationId("org-1");
    }

    @Test
    void testGetAll_HappyPath() {
        when(serviceAccountMongoDbRepository.findAll()).thenReturn(Collections.singletonList(serviceAccount));

        List<ServiceAccount> result = serviceAccountService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(serviceAccount, result.get(0));
    }

    @Test
    void testGetAll_EmptyList() {
        when(serviceAccountMongoDbRepository.findAll()).thenReturn(Collections.emptyList());

        List<ServiceAccount> result = serviceAccountService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByOrganizationId_HappyPath() {
        when(serviceAccountMongoDbRepository.findByOrganizationId("org-1")).thenReturn(Collections.singletonList(serviceAccount));

        List<ServiceAccount> result = serviceAccountService.getByOrganizationId("org-1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(serviceAccount, result.get(0));
    }

    @Test
    void testGetByOrganizationId_EmptyList() {
        when(serviceAccountMongoDbRepository.findByOrganizationId("org-1")).thenReturn(Collections.emptyList());

        List<ServiceAccount> result = serviceAccountService.getByOrganizationId("org-1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGet_HappyPath() {
        when(serviceAccountMongoDbRepository.findById("1")).thenReturn(Optional.of(serviceAccount));

        Optional<ServiceAccount> result = serviceAccountService.get("1");

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(serviceAccount, result.get());
    }

    @Test
    void testGet_NotFound() {
        when(serviceAccountMongoDbRepository.findById("2")).thenReturn(Optional.empty());

        Optional<ServiceAccount> result = serviceAccountService.get("2");

        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void testSave_HappyPath() {
        when(serviceAccountMongoDbRepository.save(serviceAccount)).thenReturn(serviceAccount);

        ServiceAccount result = serviceAccountService.save(serviceAccount);

        assertNotNull(result);
        assertEquals(serviceAccount, result);
    }

    @Test
    void testUpdate_HappyPath() {
        when(serviceAccountMongoDbRepository.save(serviceAccount)).thenReturn(serviceAccount);

        serviceAccountService.update(serviceAccount);

        verify(serviceAccountMongoDbRepository, times(1)).save(serviceAccount);
    }

    @Test
    void testDelete_HappyPath() {
        doNothing().when(serviceAccountMongoDbRepository).deleteById("1");

        serviceAccountService.delete("1");

        verify(serviceAccountMongoDbRepository, times(1)).deleteById("1");
    }
}