package com.b1.mysawit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryFeatureInterceptorTest {

    @Mock private FeatureFlags featureFlags;
    @InjectMocks private DeliveryFeatureInterceptor interceptor;

    @Test
    void whenDeliveryEnabled_preHandle_returnsTrue() throws Exception {
        when(featureFlags.isDeliveryEnabled()).thenReturn(true);

        boolean result = interceptor.preHandle(
                mock(HttpServletRequest.class), mock(HttpServletResponse.class), null);

        assertThat(result).isTrue();
    }

    @Test
    void whenDeliveryDisabled_preHandle_returnsFalse_andWrites503() throws Exception {
        when(featureFlags.isDeliveryEnabled()).thenReturn(false);

        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        boolean result = interceptor.preHandle(mock(HttpServletRequest.class), response, null);

        assertThat(result).isFalse();
        verify(response).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        verify(response).setContentType("application/json");
        verify(writer).write(anyString());
    }
}
