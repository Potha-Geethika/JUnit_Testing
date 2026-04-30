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
        district.setName("District 1");
        district.setOrganizationId("Org1");
    }

    @Test
    void testGetAll() {
        when(districtRepository.findAll()).thenReturn(Collections.singletonList(district));

        List<District> districts = districtService.getAll();

        assertNotNull(districts);
        assertEquals(1, districts.size());
        assertEquals("District 1", districts.get(0).getName());
    }

    @Test
    void testGetByOrganizationId() {
        when(districtRepository.findByOrganizationId("Org1")).thenReturn(Collections.singletonList(district));

        List<District> districts = districtService.getByOrganizationId("Org1");

        assertNotNull(districts);
        assertEquals(1, districts.size());
        assertEquals("District 1", districts.get(0).getName());
    }

    @Test
    void testGet() {
        when(districtRepository.findById("1")).thenReturn(Optional.of(district));

        Optional<District> foundDistrict = districtService.get("1");

        assertNotNull(foundDistrict);
        assertEquals("District 1", foundDistrict.get().getName());
    }

    @Test
    void testSave() {
        when(districtRepository.save(district)).thenReturn(district);

        District savedDistrict = districtService.save(district);

        assertNotNull(savedDistrict);
        assertEquals("District 1", savedDistrict.getName());
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
    void testGetByOrganizationIdEmpty() {
        when(districtRepository.findByOrganizationId("Org2")).thenReturn(Collections.emptyList());

        List<District> districts = districtService.getByOrganizationId("Org2");

        assertNotNull(districts);
        assertEquals(0, districts.size());
    }

    @Test
    void testGetNotFound() {
        when(districtRepository.findById("2")).thenReturn(Optional.empty());

        Optional<District> foundDistrict = districtService.get("2");

        assertNotNull(foundDistrict);
        assertEquals(Optional.empty(), foundDistrict);
    }
}