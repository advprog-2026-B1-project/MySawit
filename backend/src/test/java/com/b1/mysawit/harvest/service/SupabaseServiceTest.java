package com.b1.mysawit.harvest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupabaseServiceTest {

    @InjectMocks
    private SupabaseService supabaseService;

    @Test
    void uploadPhoto_shouldReturnPublicUrl() throws Exception {

        // =====================
        // Arrange
        // =====================
        String supabaseUrl = "https://xyz.supabase.co";
        String bucket = "hasil-panen-photos";
        String fileName = "test.jpg";

        ReflectionTestUtils.setField(supabaseService, "supabaseUrl", supabaseUrl);
        ReflectionTestUtils.setField(supabaseService, "supabaseKey", "test-key");
        ReflectionTestUtils.setField(supabaseService, "bucketName", bucket);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                "image/jpeg",
                "dummy-image-content".getBytes()
        );

        // Mock WebClient chain
        WebClient webClientMock = mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        try (MockedStatic<WebClient> mockedStatic = mockStatic(WebClient.class)) {

            mockedStatic.when(() -> WebClient.create(supabaseUrl))
                    .thenReturn(webClientMock);

            when(webClientMock.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
            when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("ok"));

            // =====================
            // Act
            // =====================
            String result = supabaseService.uploadPhoto(file);

            // =====================
            // Assert
            // =====================
            assertNotNull(result);
            assertTrue(result.contains(supabaseUrl));
            assertTrue(result.contains(bucket));
            assertTrue(result.endsWith(fileName));
        }
    }

    @Test
    void uploadPhoto_shouldThrowIOException() throws Exception {

        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");

        when(file.getBytes()).thenThrow(new IOException("disk error"));

        SupabaseService service = new SupabaseService();

        ReflectionTestUtils.setField(service, "supabaseUrl", "http://test");
        ReflectionTestUtils.setField(service, "supabaseKey", "key");
        ReflectionTestUtils.setField(service, "bucketName", "bucket");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.uploadPhoto(file));

        assertTrue(ex.getMessage().contains("Gagal membaca data file foto"));
    }

    @Test
    void uploadPhoto_shouldThrowWebClientException() throws Exception {

        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getBytes()).thenReturn("data".getBytes());

        WebClient webClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(any())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(
                        WebClientResponseException.create(
                                500, "error", null, null, null)));

        try (MockedStatic<WebClient> mocked = mockStatic(WebClient.class)) {

            mocked.when(() -> WebClient.create(anyString()))
                    .thenReturn(webClient);

            SupabaseService service = new SupabaseService();

            ReflectionTestUtils.setField(service, "supabaseUrl", "http://test");
            ReflectionTestUtils.setField(service, "supabaseKey", "key");
            ReflectionTestUtils.setField(service, "bucketName", "bucket");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.uploadPhoto(file));

            assertTrue(ex.getMessage().contains("Gagal upload ke Supabase"));
        }
    }
}