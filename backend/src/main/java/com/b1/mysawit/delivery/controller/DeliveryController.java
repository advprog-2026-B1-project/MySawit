package com.b1.mysawit.delivery.controller;

import com.b1.mysawit.delivery.dto.AdminDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.CreateDeliveryRequest;
import com.b1.mysawit.delivery.dto.DeliveryResponse;
import com.b1.mysawit.delivery.dto.MandorDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.UpdateDeliveryStatusRequest;
import com.b1.mysawit.delivery.service.DeliveryService;
import com.b1.mysawit.delivery.service.DeliveryAuthenticationHelper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryAuthenticationHelper authenticationHelper;

    public DeliveryController(DeliveryService deliveryService, DeliveryAuthenticationHelper authenticationHelper) {
        this.deliveryService = deliveryService;
        this.authenticationHelper = authenticationHelper;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('Mandor')")
    public ResponseEntity<DeliveryResponse> createDelivery(
            @RequestBody CreateDeliveryRequest request
    ) {
        Long mandorId = authenticationHelper.getCurrentUser().getId();
        DeliveryResponse response = deliveryService.createDelivery(mandorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DeliveryResponse>> getDeliveries() {
        return ResponseEntity.ok(deliveryService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliveryResponse> getDeliveryDetail(@PathVariable("id") Long deliveryId) {
        return ResponseEntity.ok(deliveryService.getDeliveryDetail(deliveryId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('Supir')")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable("id") Long deliveryId,
            @RequestBody UpdateDeliveryStatusRequest request
    ) {
        Long driverId = authenticationHelper.getCurrentUser().getId();
        return ResponseEntity.ok(deliveryService.updateStatus(driverId, deliveryId, request));
    }

    @PutMapping("/{id}/mandor-decision")
    @PreAuthorize("hasAuthority('Mandor')")
    public ResponseEntity<DeliveryResponse> decideByMandor(
            @PathVariable("id") Long deliveryId,
            @RequestBody MandorDeliveryDecisionRequest request
    ) {
        Long mandorId = authenticationHelper.getCurrentUser().getId();
        return ResponseEntity.ok(deliveryService.decideByMandor(mandorId, deliveryId, request));
    }

    @PutMapping("/{id}/admin-decision")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<DeliveryResponse> decideByAdmin(
            @PathVariable("id") Long deliveryId,
            @RequestBody AdminDeliveryDecisionRequest request
    ) {
        return ResponseEntity.ok(deliveryService.decideByAdmin(deliveryId, request));
    }

    @GetMapping("/driver")
    @PreAuthorize("hasAuthority('Supir')")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByDriver() {
        Long driverId = authenticationHelper.getCurrentUser().getId();
        return ResponseEntity.ok(deliveryService.getByDriver(driverId));
    }

    @GetMapping("/mandor")
    @PreAuthorize("hasAuthority('Mandor')")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByMandor() {
        Long mandorId = authenticationHelper.getCurrentUser().getId();
        return ResponseEntity.ok(deliveryService.getByMandor(mandorId));
    }

    @GetMapping("/mandor/ongoing")
    @PreAuthorize("hasAuthority('Mandor')")
    public ResponseEntity<List<DeliveryResponse>> getMandorOngoingDeliveries(
            @RequestParam(value = "driverId", required = false) Long driverId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Long mandorId = authenticationHelper.getCurrentUser().getId();
        return ResponseEntity.ok(deliveryService.getMandorOngoing(mandorId, driverId, date, keyword));
    }

    @GetMapping("/mandor/history")
    @PreAuthorize("hasAuthority('Mandor')")
    public ResponseEntity<List<DeliveryResponse>> getMandorHistoryDeliveries(
            @RequestParam(value = "driverId", required = false) Long driverId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Long mandorId = authenticationHelper.getCurrentUser().getId();
        return ResponseEntity.ok(deliveryService.getMandorHistory(mandorId, driverId, date, keyword));
    }

    @GetMapping("/admin/ready")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<List<DeliveryResponse>> getAdminReadyDeliveries(
            @RequestParam(value = "mandorName", required = false) String mandorName,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(deliveryService.getAdminReadyDeliveries(mandorName, date));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        HttpStatus status = exception.getMessage() != null && exception.getMessage().toLowerCase().contains("not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(org.springframework.security.access.AccessDeniedException exception) {
        HttpStatus status = exception.getMessage() != null && exception.getMessage().toLowerCase().contains("required")
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(Map.of("error", exception.getMessage()));
    }
}
