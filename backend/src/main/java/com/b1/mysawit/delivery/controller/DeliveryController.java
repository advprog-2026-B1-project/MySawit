package com.b1.mysawit.delivery.controller;

import com.b1.mysawit.delivery.dto.AdminDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.CreateDeliveryRequest;
import com.b1.mysawit.delivery.dto.DeliveryResponse;
import com.b1.mysawit.delivery.dto.MandorDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.UpdateDeliveryStatusRequest;
import com.b1.mysawit.delivery.service.DeliveryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(
            @RequestParam("mandorId") Long mandorId,
            @RequestBody CreateDeliveryRequest request
    ) {
        DeliveryResponse response = deliveryService.createDelivery(mandorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> getDeliveries() {
        return ResponseEntity.ok(deliveryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryDetail(@PathVariable("id") Long deliveryId) {
        return ResponseEntity.ok(deliveryService.getDeliveryDetail(deliveryId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable("id") Long deliveryId,
            @RequestParam("driverId") Long driverId,
            @RequestBody UpdateDeliveryStatusRequest request
    ) {
        return ResponseEntity.ok(deliveryService.updateStatus(driverId, deliveryId, request));
    }

    @PutMapping("/{id}/mandor-decision")
    public ResponseEntity<DeliveryResponse> decideByMandor(
            @PathVariable("id") Long deliveryId,
            @RequestParam("mandorId") Long mandorId,
            @RequestBody MandorDeliveryDecisionRequest request
    ) {
        return ResponseEntity.ok(deliveryService.decideByMandor(mandorId, deliveryId, request));
    }

    @PutMapping("/{id}/admin-decision")
    public ResponseEntity<DeliveryResponse> decideByAdmin(
            @PathVariable("id") Long deliveryId,
            @RequestBody AdminDeliveryDecisionRequest request
    ) {
        return ResponseEntity.ok(deliveryService.decideByAdmin(deliveryId, request));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByDriver(@PathVariable("driverId") Long driverId) {
        return ResponseEntity.ok(deliveryService.getByDriver(driverId));
    }

    @GetMapping("/mandor/{mandorId}")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByMandor(@PathVariable("mandorId") Long mandorId) {
        return ResponseEntity.ok(deliveryService.getByMandor(mandorId));
    }

    @GetMapping("/mandor/{mandorId}/ongoing")
    public ResponseEntity<List<DeliveryResponse>> getMandorOngoingDeliveries(
            @PathVariable("mandorId") Long mandorId,
            @RequestParam(value = "driverId", required = false) Long driverId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ResponseEntity.ok(deliveryService.getMandorOngoing(mandorId, driverId, date, keyword));
    }

    @GetMapping("/mandor/{mandorId}/history")
    public ResponseEntity<List<DeliveryResponse>> getMandorHistoryDeliveries(
            @PathVariable("mandorId") Long mandorId,
            @RequestParam(value = "driverId", required = false) Long driverId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ResponseEntity.ok(deliveryService.getMandorHistory(mandorId, driverId, date, keyword));
    }

    @GetMapping("/admin/ready")
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
}
