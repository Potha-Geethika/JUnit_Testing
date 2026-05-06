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
        serviceAccount.setOrganizationId("org1");
    }

    @Test
    void testGetAll() {
        when(serviceAccountMongoDbRepository.findAll()).thenReturn(Collections.singletonList(serviceAccount));

        List<ServiceAccount> result = serviceAccountService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(serviceAccount, result.get(0));
    }

    @Test
    void testGetByOrganizationId() {
        when(serviceAccountMongoDbRepository.findByOrganizationId("org1")).thenReturn(Collections.singletonList(serviceAccount));

        List<ServiceAccount> result = serviceAccountService.getByOrganizationId("org1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(serviceAccount, result.get(0));
    }

    @Test
    void testGet() {
        when(serviceAccountMongoDbRepository.findById("1")).thenReturn(Optional.of(serviceAccount));

        Optional<ServiceAccount> result = serviceAccountService.get("1");

        assertNotNull(result);
        assertEquals(serviceAccount, result.get());
    }

    @Test
    void testSave() {
        when(serviceAccountMongoDbRepository.save(serviceAccount)).thenReturn(serviceAccount);

        ServiceAccount result = serviceAccountService.save(serviceAccount);

        assertNotNull(result);
        assertEquals(serviceAccount, result);
    }

    @Test
    void testUpdate() {
        serviceAccountService.update(serviceAccount);
        verify(serviceAccountMongoDbRepository, times(1)).save(serviceAccount);
    }

    @Test
    void testDelete() {
        serviceAccountService.delete("1");
        verify(serviceAccountMongoDbRepository, times(1)).deleteById("1");
    }

    @Test
    void testGetAllEmpty() {
        when(serviceAccountMongoDbRepository.findAll()).thenReturn(Collections.emptyList());

        List<ServiceAccount> result = serviceAccountService.getAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetByOrganizationIdEmpty() {
        when(serviceAccountMongoDbRepository.findByOrganizationId("unknown")).thenReturn(Collections.emptyList());

        List<ServiceAccount> result = serviceAccountService.getByOrganizationId("unknown");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetNotFound() {
        when(serviceAccountMongoDbRepository.findById("unknown")).thenReturn(Optional.empty());

        Optional<ServiceAccount> result = serviceAccountService.get("unknown");

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }
}