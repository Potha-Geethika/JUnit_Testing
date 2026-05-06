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
        MockitoAnnotations.openMocks(this);
        serviceAccount = new ServiceAccount();
        serviceAccount.setId("1");
        serviceAccount.setOrganizationId("org-1");
    }

    @Test
    void testGetAll() {
        List<ServiceAccount> expectedList = Collections.singletonList(serviceAccount);
        when(serviceAccountMongoDbRepository.findAll()).thenReturn(expectedList);

        List<ServiceAccount> result = serviceAccountService.getAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    void testGetByOrganizationId() {
        String organizationId = "org-1";
        List<ServiceAccount> expectedList = Collections.singletonList(serviceAccount);
        when(serviceAccountMongoDbRepository.findByOrganizationId(organizationId)).thenReturn(expectedList);

        List<ServiceAccount> result = serviceAccountService.getByOrganizationId(organizationId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    @Test
    void testGet() {
        String serviceAccountId = "1";
        when(serviceAccountMongoDbRepository.findById(serviceAccountId)).thenReturn(Optional.of(serviceAccount));

        Optional<ServiceAccount> result = serviceAccountService.get(serviceAccountId);
        
        assertNotNull(result);
        assertEquals("1", result.get().getId());
    }

    @Test
    void testSave() {
        when(serviceAccountMongoDbRepository.save(serviceAccount)).thenReturn(serviceAccount);

        ServiceAccount result = serviceAccountService.save(serviceAccount);
        
        assertNotNull(result);
        assertEquals("1", result.getId());
    }

    @Test
    void testUpdate() {
        serviceAccountService.update(serviceAccount);
        
        verify(serviceAccountMongoDbRepository, times(1)).save(serviceAccount);
    }

    @Test
    void testDelete() {
        String serviceAccountId = "1";
        serviceAccountService.delete(serviceAccountId);
        
        verify(serviceAccountMongoDbRepository, times(1)).deleteById(serviceAccountId);
    }
}