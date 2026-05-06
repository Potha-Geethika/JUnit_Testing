package com.carbo.admin.kafka;

import java.io.*;
import java.nio.file.*;
import java.security.Principal;
import java.util.*;
import java.util.Arrays;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;





public class ProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private Producer producer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void push_ShouldSendMessage_WhenCalledWithValidParameters() {
        String topic = "test-topic";
        Object value = new Object();
        
        doNothing().when(kafkaTemplate).send(topic, value);

        producer.push(topic, value);

        verify(kafkaTemplate).send(topic, value);
    }

    @Test
    void push_ShouldHandleException_WhenKafkaTemplateThrowsException() {
        String topic = "test-topic";
        Object value = new Object();

        when(kafkaTemplate.send(topic, value)).thenThrow(new RuntimeException("Kafka error"));

        producer.push(topic, value);

        // Verify that the send method was called even though it threw an exception
        verify(kafkaTemplate).send(topic, value);
    }
}