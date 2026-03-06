package com.b1.mysawit.delivery.dto;

import com.b1.mysawit.domain.Delivery;

public record UpdateDeliveryStatusRequest(
        Delivery.Status status
) {
}
