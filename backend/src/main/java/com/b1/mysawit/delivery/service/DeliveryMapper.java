package com.b1.mysawit.delivery.service;

import com.b1.mysawit.delivery.dto.DeliveryResponse;
import com.b1.mysawit.domain.Delivery;
import org.springframework.stereotype.Component;

import java.util.List;

// Maps Delivery entities to DeliveryResponse DTOs for API responses.
@Component
public class DeliveryMapper {

    public DeliveryResponse toResponse(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getDriver() == null ? null : delivery.getDriver().getId(),
                delivery.getMandor() == null ? null : delivery.getMandor().getId(),
                delivery.getHasilPanen() == null ? null : delivery.getHasilPanen().getId(),
                delivery.getStatus(),
                delivery.getMandorDecision(),
                delivery.getAdminDecision(),
                delivery.getAcknowledgedKg(),
                delivery.getMandorRejectionReason(),
                delivery.getAdminRejectionReason(),
                delivery.getArrivedAt(),
                delivery.getMandorDecidedAt(),
                delivery.getAdminDecidedAt(),
                delivery.getCreatedAt(),
                delivery.getUpdatedAt()
        );
    }

    public List<DeliveryResponse> toResponseList(List<Delivery> deliveries) {
        return deliveries.stream().map(this::toResponse).toList();
    }
}
