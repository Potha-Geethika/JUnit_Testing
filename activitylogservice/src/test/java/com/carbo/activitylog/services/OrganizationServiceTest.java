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
        organizationService = new OrganizationService(organizationRepository);
    }

    @Test
    void testGetAll() {
        Organization org = new Organization();
        org.setId("1");
        org.setName("Test Organization");
        
        when(organizationRepository.findAll()).thenReturn(Collections.singletonList(org));

        List<Organization> organizations = organizationService.getAll();
        
        assertNotNull(organizations);
        assertEquals(1, organizations.size());
        assertEquals("Test Organization", organizations.get(0).getName());
        verify(organizationRepository, times(1)).findAll();
    }

    @Test
    void testGetWhenExists() {
        Organization org = new Organization();
        org.setId("1");
        org.setName("Test Organization");
        
        when(organizationRepository.findById("1")).thenReturn(Optional.of(org));

        Optional<Organization> organization = organizationService.get("1");
        
        assertNotNull(organization);
        assertEquals("Test Organization", organization.get().getName());
        verify(organizationRepository, times(1)).findById("1");
    }

    @Test
    void testGetWhenNotExists() {
        when(organizationRepository.findById("1")).thenReturn(Optional.empty());

        Optional<Organization> organization = organizationService.get("1");
        
        assertNotNull(organization);
        assertEquals(Optional.empty(), organization);
        verify(organizationRepository, times(1)).findById("1");
    }

    @Test
    void testSave() {
        Organization org = new Organization();
        org.setId("1");
        org.setName("Test Organization");
        
        when(organizationRepository.save(org)).thenReturn(org);

        Organization savedOrganization = organizationService.save(org);
        
        assertNotNull(savedOrganization);
        assertEquals("Test Organization", savedOrganization.getName());
        verify(organizationRepository, times(1)).save(org);
    }

    @Test
    void testUpdate() {
        Organization org = new Organization();
        org.setId("1");
        org.setName("Updated Organization");

        organizationService.update(org);
        
        verify(organizationRepository, times(1)).save(org);
    }

    @Test
    void testDelete() {
        String organizationId = "1";

        organizationService.delete(organizationId);
        
        verify(organizationRepository, times(1)).deleteById(organizationId);
    }
}