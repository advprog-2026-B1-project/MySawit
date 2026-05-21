package com.b1.mysawit.delivery.dto;

import com.b1.mysawit.domain.Delivery;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DeliveryResponse(
        Long id,
        Long driverId,
        Long mandorId,
        Long hasilPanenId,
        Delivery.Status status,
        Delivery.MandorDecision mandorDecision,
        Delivery.AdminDecision adminDecision,
        BigDecimal acknowledgedKg,
        String mandorRejectionReason,
        String adminRejectionReason,
        OffsetDateTime arrivedAt,
        OffsetDateTime mandorDecidedAt,
        OffsetDateTime adminDecidedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
