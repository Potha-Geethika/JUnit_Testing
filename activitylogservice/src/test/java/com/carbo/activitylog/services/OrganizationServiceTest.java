package com.carbo.activitylog.services;
import static org.mockito.ArgumentMatchers.any;

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

    @InjectMocks
    private OrganizationService organizationService;

    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setId("1");
        organization.setName("Test Organization");
    }

    @Test
    void testGetAll_HappyPath() {
        when(organizationRepository.findAll()).thenReturn(Collections.singletonList(organization));

        List<Organization> result = organizationService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(organization.getId(), result.get(0).getId());
    }

    @Test
    void testGetAll_EmptyList() {
        when(organizationRepository.findAll()).thenReturn(Collections.emptyList());

        List<Organization> result = organizationService.getAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGet_HappyPath() {
        when(organizationRepository.findById("1")).thenReturn(Optional.of(organization));

        Optional<Organization> result = organizationService.get("1");

        assertNotNull(result);
        assertEquals(organization.getId(), result.get().getId());
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
        when(organizationRepository.save(organization)).thenReturn(organization);

        Organization result = organizationService.save(organization);

        assertNotNull(result);
        assertEquals(organization.getId(), result.getId());
    }

    @Test
    void testUpdate_HappyPath() {
        doNothing().when(organizationRepository).save(organization);

        organizationService.update(organization);

        verify(organizationRepository, times(1)).save(organization);
    }

    @Test
    void testDelete_HappyPath() {
        doNothing().when(organizationRepository).deleteById("1");

        organizationService.delete("1");

        verify(organizationRepository, times(1)).deleteById("1");
    }

    @Test
    void testSave_ValidationError() {
        Organization invalidOrganization = new Organization();
        invalidOrganization.setId("2"); // no name set

        // Assuming the repository throws an exception if the organization is invalid
        when(organizationRepository.save(invalidOrganization)).thenThrow(new RuntimeException("Validation error"));

        try {
            organizationService.save(invalidOrganization);
        } catch (RuntimeException e) {
            assertEquals("Validation error", e.getMessage());
        }

        verify(organizationRepository, times(1)).save(invalidOrganization);
    }

    @Test
    void testUpdate_NullOrganization() {
        organizationService.update(null);
        verify(organizationRepository, times(0)).save(any());
    }
}