package com.b1.mysawit.repository;

import com.b1.mysawit.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findAllByDriver_IdOrderByUpdatedAtDesc(Long driverId);

    List<Delivery> findAllByMandor_IdOrderByUpdatedAtDesc(Long mandorId);

    List<Delivery> findAllByMandorDecisionOrderByMandorDecidedAtDesc(
            Delivery.MandorDecision mandorDecision
    );

    default List<Delivery> findByDriver(Long driverId) {
        return findAllByDriver_IdOrderByUpdatedAtDesc(driverId);
    }

    default List<Delivery> findByMandor(Long mandorId) {
        return findAllByMandor_IdOrderByUpdatedAtDesc(mandorId);
    }

    default List<Delivery> findForAdminReview(Delivery.MandorDecision mandorDecision) {
        return findAllByMandorDecisionOrderByMandorDecidedAtDesc(mandorDecision);
    }
}
