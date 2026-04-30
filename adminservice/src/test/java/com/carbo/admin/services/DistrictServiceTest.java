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
        district.setColor("Blue");
    }

    @Test
    void getAll() {
        List<District> expectedList = Collections.singletonList(district);
        when(districtRepository.findAll()).thenReturn(expectedList);

        List<District> result = districtService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedList.get(0).getName(), result.get(0).getName());
    }

    @Test
    void getByOrganizationId() {
        List<District> expectedList = Collections.singletonList(district);
        when(districtRepository.findByOrganizationId("Org1")).thenReturn(expectedList);

        List<District> result = districtService.getByOrganizationId("Org1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedList.get(0).getName(), result.get(0).getName());
    }

    @Test
    void get() {
        when(districtRepository.findById("1")).thenReturn(Optional.of(district));

        Optional<District> result = districtService.get("1");

        assertNotNull(result);
        assertEquals(district.getId(), result.get().getId());
    }

    @Test
    void save() {
        when(districtRepository.save(district)).thenReturn(district);

        District result = districtService.save(district);

        assertNotNull(result);
        assertEquals(district.getId(), result.getId());
    }

    @Test
    void update() {
        doNothing().when(districtRepository).save(district);

        districtService.update(district);

        verify(districtRepository, times(1)).save(district);
    }

    @Test
    void delete() {
        doNothing().when(districtRepository).deleteById("1");

        districtService.delete("1");

        verify(districtRepository, times(1)).deleteById("1");
    }
}