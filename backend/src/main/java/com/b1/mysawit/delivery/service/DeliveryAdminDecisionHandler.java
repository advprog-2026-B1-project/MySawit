package com.b1.mysawit.delivery.service;

import com.b1.mysawit.delivery.dto.AdminDeliveryDecisionRequest;
import com.b1.mysawit.domain.Delivery;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DeliveryAdminDecisionHandler {

    public void applyDecision(Delivery delivery, AdminDeliveryDecisionRequest request) {
        switch (request.decision()) {
            case Approved -> {
                delivery.setAcknowledgedKg(getHarvestKgOrThrow(delivery));
                delivery.setAdminRejectionReason(null);
            }
            case Rejected -> {
                delivery.setAcknowledgedKg(BigDecimal.ZERO);
                delivery.setAdminRejectionReason(request.rejectionReason());
            }
            case PartiallyApproved -> {
                delivery.setAcknowledgedKg(request.acknowledgedKg());
                delivery.setAdminRejectionReason(request.rejectionReason());
            }
            default -> throw new IllegalArgumentException("Unsupported admin decision");
        }
    }

    public boolean isPayrollEligible(Delivery.AdminDecision decision) {
        return decision == Delivery.AdminDecision.Approved
                || decision == Delivery.AdminDecision.PartiallyApproved;
    }

    private BigDecimal getHarvestKgOrThrow(Delivery delivery) {
        if (delivery.getHasilPanen() == null || delivery.getHasilPanen().getKilogram() == null) {
            throw new IllegalArgumentException("Delivery harvest kilogram is missing");
        }
        return delivery.getHasilPanen().getKilogram();
    }
}
