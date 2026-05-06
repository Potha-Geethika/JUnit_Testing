package com.carbo.pad.utils;

import com.carbo.pad.model.ActivityLogEntry;
import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;





class ActivityLogUtilTest {

    @Test
    void testConvertToLocalDateTime_HHMMFormat() {
        String time = "14:30";
        LocalDateTime result = ActivityLogUtil.convertToLocalDateTime(time);
        assertEquals(LocalDateTime.of(LocalDate.now(), LocalTime.of(14, 30)), result);
    }

    @Test
    void testConvertToLocalDateTime_yyyyMMddHHMMFormat() {
        String time = "20230101 14:30";
        LocalDateTime result = ActivityLogUtil.convertToLocalDateTime(time);
        assertEquals(LocalDateTime.of(2023, 1, 1, 14, 30), result);
    }

    @Test
    void testConvertToLocalDateTime_NullInput() {
        LocalDateTime result = ActivityLogUtil.convertToLocalDateTime(null);
        assertEquals(null, result);
    }




    @Test
    void testRound_PositiveNumber() {
        Float result = ActivityLogUtil.round(3.14159f, 2);
        assertEquals(3.14f, result);
    }

}