package com.b1.mysawit.delivery.service;

import com.b1.mysawit.delivery.dto.AdminDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.DeliveryResponse;
import com.b1.mysawit.delivery.dto.MandorDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.UpdateDeliveryStatusRequest;
import com.b1.mysawit.domain.Delivery;
import com.b1.mysawit.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final DeliveryValidator deliveryValidator;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            DeliveryMapper deliveryMapper,
            DeliveryValidator deliveryValidator
    ) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryMapper = deliveryMapper;
        this.deliveryValidator = deliveryValidator;
    }

    public DeliveryResponse updateStatus(Long driverId, Long deliveryId, UpdateDeliveryStatusRequest request) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        deliveryValidator.validateAssignedDriver(delivery, driverId);

        Delivery.Status nextStatus = deliveryValidator.requireNextStatus(request);
        deliveryValidator.validateStatusTransition(delivery.getStatus(), nextStatus);

        delivery.setStatus(nextStatus);
        if (nextStatus == Delivery.Status.Tiba) {
            delivery.setArrivedAt(OffsetDateTime.now());
        }
        delivery.setUpdatedAt(OffsetDateTime.now());

        return deliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    public DeliveryResponse decideByMandor(Long mandorId, Long deliveryId, MandorDeliveryDecisionRequest request) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        deliveryValidator.validateMandorDecisionRequest(delivery, mandorId, request);

        delivery.setMandorDecision(request.decision());
        delivery.setMandorRejectionReason(request.decision() == Delivery.MandorDecision.Rejected
                ? request.rejectionReason()
                : null);
        delivery.setMandorDecidedAt(OffsetDateTime.now());
        delivery.setUpdatedAt(OffsetDateTime.now());

        return deliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    public DeliveryResponse decideByAdmin(Long deliveryId, AdminDeliveryDecisionRequest request) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        deliveryValidator.validateAdminDecisionRequest(delivery, request);

        applyAdminDecision(delivery, request);
        delivery.setAdminDecision(request.decision());
        delivery.setAdminDecidedAt(OffsetDateTime.now());
        delivery.setUpdatedAt(OffsetDateTime.now());

        return deliveryMapper.toResponse(deliveryRepository.save(delivery));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getByDriver(Long driverId) {
        return deliveryMapper.toResponseList(deliveryRepository.findByDriver(driverId));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getByMandor(Long mandorId) {
        return deliveryMapper.toResponseList(deliveryRepository.findByMandor(mandorId));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getForAdminReview() {
        return deliveryMapper.toResponseList(
                deliveryRepository.findForAdminReview(Delivery.MandorDecision.Approved)
        );
    }

    private void applyAdminDecision(Delivery delivery, AdminDeliveryDecisionRequest request) {
        if (request.decision() == Delivery.AdminDecision.Approved) {
            delivery.setAcknowledgedKg(delivery.getHasilPanen() == null
                    ? null
                    : delivery.getHasilPanen().getKilogram());
            delivery.setAdminRejectionReason(null);
            return;
        }

        if (request.decision() == Delivery.AdminDecision.Rejected) {
            delivery.setAcknowledgedKg(BigDecimal.ZERO);
            delivery.setAdminRejectionReason(request.rejectionReason());
            return;
        }

        delivery.setAcknowledgedKg(request.acknowledgedKg());
        delivery.setAdminRejectionReason(request.rejectionReason());
    }

    private Delivery getDeliveryOrThrow(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));
    }
}
