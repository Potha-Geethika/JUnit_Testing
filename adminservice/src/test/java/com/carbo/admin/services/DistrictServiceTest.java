package com.carbo.admin.services;
import static org.mockito.ArgumentMatchers.any;

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
    void getAll_HappyPath() {
        List<District> expectedList = Collections.singletonList(new District());
        when(districtRepository.findAll()).thenReturn(expectedList);

        List<District> result = districtService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getByOrganizationId_HappyPath() {
        String organizationId = "org123";
        List<District> expectedList = Collections.singletonList(new District());
        when(districtRepository.findByOrganizationId(organizationId)).thenReturn(expectedList);

        List<District> result = districtService.getByOrganizationId(organizationId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void get_HappyPath() {
        String districtId = "district123";
        District expectedDistrict = new District();
        when(districtRepository.findById(districtId)).thenReturn(Optional.of(expectedDistrict));

        Optional<District> result = districtService.get(districtId);

        assertNotNull(result);
        assertEquals(expectedDistrict, result.get());
    }

    @Test
    void save_HappyPath() {
        District district = new District();
        when(districtRepository.save(any(District.class))).thenReturn(district);

        District result = districtService.save(district);

        assertNotNull(result);
        assertEquals(district, result);
    }

    @Test
    void update_HappyPath() {
        District district = new District();
        doNothing().when(districtRepository).save(any(District.class));

        districtService.update(district);

        verify(districtRepository, times(1)).save(district);
    }

    @Test
    void delete_HappyPath() {
        String districtId = "district123";
        doNothing().when(districtRepository).deleteById(districtId);

        districtService.delete(districtId);

        verify(districtRepository, times(1)).deleteById(districtId);
    }

    @Test
    void getByOrganizationId_EmptyList() {
        String organizationId = "org123";
        when(districtRepository.findByOrganizationId(organizationId)).thenReturn(Collections.emptyList());

        List<District> result = districtService.getByOrganizationId(organizationId);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void get_NonExistentDistrict() {
        String districtId = "non-existent-id";
        when(districtRepository.findById(districtId)).thenReturn(Optional.empty());

        Optional<District> result = districtService.get(districtId);

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }
}