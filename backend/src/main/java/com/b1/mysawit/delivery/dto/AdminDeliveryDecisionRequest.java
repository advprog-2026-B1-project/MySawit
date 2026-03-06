package com.b1.mysawit.delivery.dto;

import com.b1.mysawit.domain.Delivery;

import java.math.BigDecimal;

public record AdminDeliveryDecisionRequest(
        Delivery.AdminDecision decision,
        BigDecimal acknowledgedKg,
        String rejectionReason
) {
}
