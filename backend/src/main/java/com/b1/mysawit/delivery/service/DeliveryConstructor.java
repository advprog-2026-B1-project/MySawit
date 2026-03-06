package com.b1.mysawit.delivery.service;

import com.b1.mysawit.domain.Delivery;
import com.b1.mysawit.domain.HasilPanen;
import com.b1.mysawit.domain.User;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class DeliveryConstructor {

    public Delivery newDelivery(User mandor, User driver, HasilPanen hasilPanen) {
        OffsetDateTime now = OffsetDateTime.now();

        Delivery delivery = new Delivery();
        delivery.setDriver(driver);
        delivery.setMandor(mandor);
        delivery.setHasilPanen(hasilPanen);
        delivery.setStatus(Delivery.Status.Memuat);
        delivery.setMandorDecision(Delivery.MandorDecision.Pending);
        delivery.setAdminDecision(Delivery.AdminDecision.Pending);
        delivery.setCreatedAt(now);
        delivery.setUpdatedAt(now);
        return delivery;
    }
}
