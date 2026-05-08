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

    private District district;

    @BeforeEach
    void setUp() {
        district = new District();
        district.setId("1");
        district.setName("Test District");
        district.setOrganizationId("org-1");
    }

    @Test
    void testGetAll() {
        List<District> expectedList = Collections.singletonList(district);
        when(districtRepository.findAll()).thenReturn(expectedList);

        List<District> result = districtService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test District", result.get(0).getName());
    }

    @Test
    void testGetByOrganizationId() {
        List<District> expectedList = Collections.singletonList(district);
        when(districtRepository.findByOrganizationId("org-1")).thenReturn(expectedList);

        List<District> result = districtService.getByOrganizationId("org-1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test District", result.get(0).getName());
    }

    @Test
    void testGet() {
        when(districtRepository.findById("1")).thenReturn(Optional.of(district));

        Optional<District> result = districtService.get("1");

        assertNotNull(result);
        assertEquals("Test District", result.get().getName());
    }

    @Test
    void testSave() {
        when(districtRepository.save(district)).thenReturn(district);

        District result = districtService.save(district);

        assertNotNull(result);
        assertEquals("Test District", result.getName());
    }

    @Test
    void testUpdate() {
        doNothing().when(districtRepository).save(district);

        districtService.update(district);

        verify(districtRepository, times(1)).save(district);
    }

    @Test
    void testDelete() {
        doNothing().when(districtRepository).deleteById("1");

        districtService.delete("1");

        verify(districtRepository, times(1)).deleteById("1");
    }

    @Test
    void testGetAllEmpty() {
        when(districtRepository.findAll()).thenReturn(Collections.emptyList());

        List<District> result = districtService.getAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetByOrganizationIdNotFound() {
        when(districtRepository.findByOrganizationId("non-existent-org")).thenReturn(Collections.emptyList());

        List<District> result = districtService.getByOrganizationId("non-existent-org");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetNotFound() {
        when(districtRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        Optional<District> result = districtService.get("non-existent-id");

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testSaveNullDistrict() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            districtService.save(null);
        });
        assertEquals("District cannot be null", exception.getMessage());
    }
}