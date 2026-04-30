package com.carbo.admin.services;
import static org.mockito.ArgumentMatchers.anyString;
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

    private District district;

    @BeforeEach
    void setUp() {
        district = new District();
        district.setId("1");
        district.setName("Test District");
        district.setOrganizationId("org-123");
        district.setColor("Blue");
    }

    @Test
    void getAll_HappyPath() {
        when(districtRepository.findAll()).thenReturn(Collections.singletonList(district));
        
        List<District> result = districtService.getAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(district, result.get(0));
    }

    @Test
    void getByOrganizationId_HappyPath() {
        when(districtRepository.findByOrganizationId(anyString())).thenReturn(Collections.singletonList(district));
        
        List<District> result = districtService.getByOrganizationId("org-123");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(district, result.get(0));
    }

    @Test
    void get_HappyPath() {
        when(districtRepository.findById(anyString())).thenReturn(Optional.of(district));
        
        Optional<District> result = districtService.get("1");
        
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(district, result.get());
    }

    @Test
    void save_HappyPath() {
        when(districtRepository.save(any(District.class))).thenReturn(district);
        
        District result = districtService.save(district);
        
        assertNotNull(result);
        assertEquals(district, result);
    }

    @Test
    void update_HappyPath() {
        doNothing().when(districtRepository).save(any(District.class));
        
        assertDoesNotThrow(() -> districtService.update(district));
        verify(districtRepository, times(1)).save(district);
    }

    @Test
    void delete_HappyPath() {
        doNothing().when(districtRepository).deleteById(anyString());
        
        assertDoesNotThrow(() -> districtService.delete("1"));
        verify(districtRepository, times(1)).deleteById("1");
    }

    @Test
    void getByOrganizationId_EmptyList() {
        when(districtRepository.findByOrganizationId(anyString())).thenReturn(Collections.emptyList());
        
        List<District> result = districtService.getByOrganizationId("org-unknown");
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void get_NotFound() {
        when(districtRepository.findById(anyString())).thenReturn(Optional.empty());
        
        Optional<District> result = districtService.get("unknown-id");
        
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void save_NullDistrict() {
        assertThrows(IllegalArgumentException.class, () -> districtService.save(null));
    }
}