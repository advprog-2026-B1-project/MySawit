package com.b1.mysawit.delivery.service;

import com.b1.mysawit.domain.Delivery;
import com.b1.mysawit.repository.DeliveryRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;

@Component
public class DriverTripLoadValidator {

    private static final BigDecimal MAX_TRIP_KG = new BigDecimal("400");

    private final DeliveryRepository deliveryRepository;

    public DriverTripLoadValidator(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    public void validateTripCapacity(
            Long driverId,
            BigDecimal newHarvestKg,
            Collection<Delivery.Status> ongoingStatuses
    ) {
        BigDecimal ongoingKg = orZero(deliveryRepository.sumOngoingKgByDriver(driverId, ongoingStatuses));
        BigDecimal currentHarvestKg = orZero(newHarvestKg);
        BigDecimal totalKg = ongoingKg.add(currentHarvestKg);

        if (totalKg.compareTo(MAX_TRIP_KG) > 0) {
            throw new IllegalArgumentException("Total delivery load for one trip cannot exceed 400kg");
        }
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
