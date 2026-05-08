package com.carbo.activitylog.services;

import com.carbo.activitylog.model.Organization;
import com.carbo.activitylog.repository.OrganizationMongoDbRepository;
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
class OrganizationServiceTest {

    @Mock
    private OrganizationMongoDbRepository organizationRepository;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        organizationService = new OrganizationService(organizationRepository);
    }

    @Test
    void testGetAll_HappyPath() {
        Organization org1 = new Organization();
        org1.setId("1");
        org1.setName("Org1");
        
        Organization org2 = new Organization();
        org2.setId("2");
        org2.setName("Org2");

        when(organizationRepository.findAll()).thenReturn(List.of(org1, org2));

        List<Organization> organizations = organizationService.getAll();

        assertNotNull(organizations);
        assertEquals(2, organizations.size());
        assertEquals("Org1", organizations.get(0).getName());
        assertEquals("Org2", organizations.get(1).getName());
    }

    @Test
    void testGetAll_EmptyList() {
        when(organizationRepository.findAll()).thenReturn(Collections.emptyList());

        List<Organization> organizations = organizationService.getAll();

        assertNotNull(organizations);
        assertEquals(0, organizations.size());
    }

    @Test
    void testGet_HappyPath() {
        Organization org = new Organization();
        org.setId("1");
        org.setName("Org1");

        when(organizationRepository.findById("1")).thenReturn(Optional.of(org));

        Optional<Organization> result = organizationService.get("1");

        assertNotNull(result);
        assertEquals("Org1", result.get().getName());
    }

    @Test
    void testGet_NotFound() {
        when(organizationRepository.findById("1")).thenReturn(Optional.empty());

        Optional<Organization> result = organizationService.get("1");

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testSave_HappyPath() {
        Organization org = new Organization();
        org.setId("1");
        org.setName("Org1");

        when(organizationRepository.save(org)).thenReturn(org);

        Organization result = organizationService.save(org);

        assertNotNull(result);
        assertEquals("Org1", result.getName());
    }

    @Test
    void testUpdate_HappyPath() {
        Organization org = new Organization();
        org.setId("1");
        org.setName("Updated Org");

        when(organizationRepository.save(org)).thenReturn(org);

        organizationService.update(org);

        verify(organizationRepository, times(1)).save(org);
    }

    @Test
    void testDelete_HappyPath() {
        String organizationId = "1";

        organizationService.delete(organizationId);

        verify(organizationRepository, times(1)).deleteById(organizationId);
    }
}