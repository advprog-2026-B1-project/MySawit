package com.b1.mysawit.delivery.controller;

import com.b1.mysawit.delivery.dto.AdminDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.CreateDeliveryRequest;
import com.b1.mysawit.delivery.dto.DeliveryResponse;
import com.b1.mysawit.delivery.dto.MandorDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.UpdateDeliveryStatusRequest;
import com.b1.mysawit.delivery.service.DeliveryAuthenticationHelper;
import com.b1.mysawit.delivery.service.DeliveryService;
import com.b1.mysawit.domain.Delivery;
import com.b1.mysawit.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private DeliveryAuthenticationHelper authenticationHelper;

    @InjectMocks
    private DeliveryController deliveryController;

    private DeliveryResponse dummyResponse;
    private User dummyMandor;
    private User dummyDriver;

    @BeforeEach
    void setUp() {
        dummyResponse = new DeliveryResponse(
                1L, 1L, 2L, 3L, Delivery.Status.Memuat, null,
                null, null, null, null, null, null, null, null, null
        );
        dummyMandor = new User();
        dummyMandor.setId(1L);
        dummyMandor.setEmail("mandor@mysawit.com");
        dummyMandor.setRole(User.Role.Mandor);

        dummyDriver = new User();
        dummyDriver.setId(2L);
        dummyDriver.setEmail("driver@mysawit.com");
        dummyDriver.setRole(User.Role.Supir);
    }

    @Test
    void createDelivery_shouldReturnCreated() {
        CreateDeliveryRequest request = new CreateDeliveryRequest(2L, 3L);
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyMandor);
        when(deliveryService.createDelivery(eq(1L), any())).thenReturn(dummyResponse);

        ResponseEntity<DeliveryResponse> response = deliveryController.createDelivery(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dummyResponse, response.getBody());
        verify(deliveryService).createDelivery(1L, request);
    }

    @Test
    void getDeliveries_shouldReturnOkAndList() {
        when(deliveryService.getAll()).thenReturn(List.of(dummyResponse));

        ResponseEntity<List<DeliveryResponse>> response = deliveryController.getDeliveries();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(deliveryService).getAll();
    }

    @Test
    void getDeliveryDetail_shouldReturnOkAndDetail() {
        when(deliveryService.getDeliveryDetail(1L)).thenReturn(dummyResponse);

        ResponseEntity<DeliveryResponse> response = deliveryController.getDeliveryDetail(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dummyResponse, response.getBody());
        verify(deliveryService).getDeliveryDetail(1L);
    }

    @Test
    void updateStatus_shouldReturnOk() {
        UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest(Delivery.Status.Mengirim);
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyDriver);
        when(deliveryService.updateStatus(eq(2L), eq(1L), any())).thenReturn(dummyResponse);

        ResponseEntity<DeliveryResponse> response = deliveryController.updateStatus(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dummyResponse, response.getBody());
        verify(deliveryService).updateStatus(2L, 1L, request);
    }

    @Test
    void decideByMandor_shouldReturnOk() {
        MandorDeliveryDecisionRequest request = new MandorDeliveryDecisionRequest(Delivery.MandorDecision.Approved, null);
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyMandor);
        when(deliveryService.decideByMandor(eq(1L), eq(2L), any())).thenReturn(dummyResponse);

        ResponseEntity<DeliveryResponse> response = deliveryController.decideByMandor(2L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dummyResponse, response.getBody());
        verify(deliveryService).decideByMandor(1L, 2L, request);
    }

    @Test
    void decideByAdmin_shouldReturnOk() {
        AdminDeliveryDecisionRequest request = new AdminDeliveryDecisionRequest(Delivery.AdminDecision.Approved, null, null);
        when(deliveryService.decideByAdmin(eq(1L), any())).thenReturn(dummyResponse);

        ResponseEntity<DeliveryResponse> response = deliveryController.decideByAdmin(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dummyResponse, response.getBody());
        verify(deliveryService).decideByAdmin(1L, request);
    }

    @Test
    void getDeliveriesByDriver_shouldReturnOk() {
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyDriver);
        when(deliveryService.getByDriver(2L)).thenReturn(List.of(dummyResponse));

        ResponseEntity<List<DeliveryResponse>> response = deliveryController.getDeliveriesByDriver();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryService).getByDriver(2L);
    }

    @Test
    void getDeliveriesByMandor_shouldReturnOk() {
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyMandor);
        when(deliveryService.getByMandor(1L)).thenReturn(List.of(dummyResponse));

        ResponseEntity<List<DeliveryResponse>> response = deliveryController.getDeliveriesByMandor();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryService).getByMandor(1L);
    }

    @Test
    void getMandorOngoingDeliveries_shouldReturnOk() {
        LocalDate date = LocalDate.now();
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyMandor);
        when(deliveryService.getMandorOngoing(1L, 2L, date, "test")).thenReturn(List.of(dummyResponse));

        ResponseEntity<List<DeliveryResponse>> response = deliveryController.getMandorOngoingDeliveries(2L, date, "test");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryService).getMandorOngoing(1L, 2L, date, "test");
    }

    @Test
    void getMandorHistoryDeliveries_shouldReturnOk() {
        LocalDate date = LocalDate.now();
        when(authenticationHelper.getCurrentUser()).thenReturn(dummyMandor);
        when(deliveryService.getMandorHistory(1L, 2L, date, "test")).thenReturn(List.of(dummyResponse));

        ResponseEntity<List<DeliveryResponse>> response = deliveryController.getMandorHistoryDeliveries(2L, date, "test");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryService).getMandorHistory(1L, 2L, date, "test");
    }

    @Test
    void getAdminReadyDeliveries_shouldReturnOk() {
        LocalDate date = LocalDate.now();
        when(deliveryService.getAdminReadyDeliveries("mandor", date)).thenReturn(List.of(dummyResponse));

        ResponseEntity<List<DeliveryResponse>> response = deliveryController.getAdminReadyDeliveries("mandor", date);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryService).getAdminReadyDeliveries("mandor", date);
    }

    @Test
    void handleIllegalArgument_shouldReturnNotFound_whenMessageContainsNotFound() {
        IllegalArgumentException ex = new IllegalArgumentException("Delivery not found");
        ResponseEntity<Map<String, String>> response = deliveryController.handleIllegalArgument(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Delivery not found", response.getBody().get("error"));
    }

    @Test
    void handleIllegalArgument_shouldReturnBadRequest_whenMessageDoesNotContainNotFound() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid status");
        ResponseEntity<Map<String, String>> response = deliveryController.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid status", response.getBody().get("error"));
    }

    @Test
    void handleAccessDenied_shouldReturnUnauthorized_whenAuthenticationRequired() {
        AccessDeniedException ex = new AccessDeniedException("Authentication required");
        ResponseEntity<Map<String, String>> response = deliveryController.handleAccessDenied(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication required", response.getBody().get("error"));
    }

    @Test
    void handleAccessDenied_shouldReturnForbidden_whenAccessIsDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");
        ResponseEntity<Map<String, String>> response = deliveryController.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access is denied", response.getBody().get("error"));
    }
}