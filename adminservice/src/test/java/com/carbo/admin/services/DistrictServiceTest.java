package com.carbo.admin.services;

import com.carbo.admin.model.District;
import com.carbo.admin.repository.DistrictMongoDbRepository;
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
class DistrictServiceTest {

    @Mock
    private DistrictMongoDbRepository districtRepository;

    @InjectMocks
    private DistrictService districtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        District district = new District();
        district.setId("1");
        district.setName("District A");
        when(districtRepository.findAll()).thenReturn(Collections.singletonList(district));

        List<District> result = districtService.getAll();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("District A", result.get(0).getName());
    }

    @Test
    void testGetByOrganizationId() {
        District district = new District();
        district.setId("1");
        district.setName("District A");
        when(districtRepository.findByOrganizationId("org1")).thenReturn(Collections.singletonList(district));

        List<District> result = districtService.getByOrganizationId("org1");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("District A", result.get(0).getName());
    }

    @Test
    void testGetByOrganizationIdEmpty() {
        when(districtRepository.findByOrganizationId("org2")).thenReturn(Collections.emptyList());

        List<District> result = districtService.getByOrganizationId("org2");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGet() {
        District district = new District();
        district.setId("1");
        when(districtRepository.findById("1")).thenReturn(Optional.of(district));

        Optional<District> result = districtService.get("1");
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
    }

    @Test
    void testGetNotFound() {
        when(districtRepository.findById("2")).thenReturn(Optional.empty());

        Optional<District> result = districtService.get("2");
        assertFalse(result.isPresent());
    }

    @Test
    void testSave() {
        District district = new District();
        district.setId("1");
        district.setName("District A");
        when(districtRepository.save(district)).thenReturn(district);

        District result = districtService.save(district);
        assertNotNull(result);
        assertEquals("District A", result.getName());
    }

    @Test
    void testUpdate() {
        District district = new District();
        district.setId("1");
        district.setName("District A");
        
        districtService.update(district);
        verify(districtRepository, times(1)).save(district);
    }

    @Test
    void testDelete() {
        String districtId = "1";
        districtService.delete(districtId);
        verify(districtRepository, times(1)).deleteById(districtId);
    }
}