package com.b1.mysawit.delivery.service;

import com.b1.mysawit.delivery.dto.AdminDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.CreateDeliveryRequest;
import com.b1.mysawit.delivery.dto.DeliveryResponse;
import com.b1.mysawit.delivery.dto.MandorDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.UpdateDeliveryStatusRequest;
import com.b1.mysawit.domain.Delivery;
import com.b1.mysawit.domain.HasilPanen;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.DeliveryRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeliveryService {

    private static final Collection<Delivery.Status> ONGOING_STATUSES = List.of(
            Delivery.Status.Memuat,
            Delivery.Status.Mengirim
    );

    private static final Collection<Delivery.Status> HISTORY_STATUSES = List.of(Delivery.Status.Tiba);

    private static final Collection<Delivery.Status> ALL_DELIVERY_STATUSES = List.of(
            Delivery.Status.Memuat,
            Delivery.Status.Mengirim,
            Delivery.Status.Tiba
    );

    private static final String DELIVERY_NOT_FOUND = "Delivery not found";
    private static final String MANDOR_DELIVERY_NOT_FOUND = "Delivery not found for this mandor";
    private static final String DRIVER_DELIVERY_NOT_FOUND = "Delivery not found for this driver";
    private static final String MANDOR_NOT_FOUND = "Mandor not found";
    private static final String DRIVER_NOT_FOUND = "Driver not found";
    private static final String HARVEST_NOT_FOUND = "Harvest not found";

    private final DeliveryRepository deliveryRepository;
    private final EntityManager entityManager;
    private final DeliveryMapper deliveryMapper;
    private final DeliveryValidator deliveryValidator;
    private final DeliveryConstructor deliveryConstructor;
    private final DriverTripLoadValidator driverTripLoadValidator;
    private final DeliveryAdminDecisionHandler deliveryAdminDecisionHandler;
    private final TimeProvider timeProvider;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            EntityManager entityManager,
            DeliveryMapper deliveryMapper,
            DeliveryValidator deliveryValidator,
            DeliveryConstructor deliveryConstructor,
            DriverTripLoadValidator driverTripLoadValidator,
            DeliveryAdminDecisionHandler deliveryAdminDecisionHandler,
            TimeProvider timeProvider
    ) {
        this.deliveryRepository = deliveryRepository;
        this.entityManager = entityManager;
        this.deliveryMapper = deliveryMapper;
        this.deliveryValidator = deliveryValidator;
        this.deliveryConstructor = deliveryConstructor;
        this.driverTripLoadValidator = driverTripLoadValidator;
        this.deliveryAdminDecisionHandler = deliveryAdminDecisionHandler;
        this.timeProvider = timeProvider;
    }

    public DeliveryResponse createDelivery(Long mandorId, CreateDeliveryRequest request) {
        requireCreateRequest(request);

        User mandor = getUserOrThrow(mandorId, MANDOR_NOT_FOUND);
        User driver = getUserOrThrow(request.driverId(), DRIVER_NOT_FOUND);
        HasilPanen hasilPanen = getHasilPanenOrThrow(request.hasilPanenId());

        deliveryValidator.validateCreateDelivery(mandor, driver, hasilPanen);

        if (deliveryRepository.existsByHasilPanen_IdAndStatusIn(request.hasilPanenId(), ALL_DELIVERY_STATUSES)) {
            throw new IllegalArgumentException("Harvest has already been assigned to a delivery");
        }

        driverTripLoadValidator.validateTripCapacity(driver.getId(), hasilPanen.getKilogram(), ONGOING_STATUSES);

        Delivery delivery = deliveryConstructor.newDelivery(mandor, driver, hasilPanen);
        return toResponse(deliveryRepository.save(delivery));
    }

    public DeliveryResponse updateStatus(Long driverId, Long deliveryId, UpdateDeliveryStatusRequest request) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        deliveryValidator.validateAssignedDriver(delivery, driverId);

        Delivery.Status nextStatus = deliveryValidator.requireNextStatus(request);
        deliveryValidator.validateStatusTransition(delivery.getStatus(), nextStatus);

        OffsetDateTime currentTime = now();
        delivery.setStatus(nextStatus);
        if (nextStatus == Delivery.Status.Tiba) {
            delivery.setArrivedAt(currentTime);
        }
        delivery.setUpdatedAt(currentTime);

        return toResponse(deliveryRepository.save(delivery));
    }

    public DeliveryResponse decideByMandor(Long mandorId, Long deliveryId, MandorDeliveryDecisionRequest request) {
        Delivery delivery = getMandorDeliveryOrThrow(deliveryId, mandorId);
        deliveryValidator.validateMandorDecisionRequest(delivery, mandorId, request);

        delivery.setMandorDecision(request.decision());
        delivery.setMandorRejectionReason(request.decision() == Delivery.MandorDecision.Rejected
                ? request.rejectionReason()
                : null);

        if (request.decision() == Delivery.MandorDecision.Approved) {
            markDriverPayrollQueued(delivery);
        }

        OffsetDateTime currentTime = now();
        delivery.setMandorDecidedAt(currentTime);
        delivery.setUpdatedAt(currentTime);

        return toResponse(deliveryRepository.save(delivery));
    }

    public DeliveryResponse decideByAdmin(Long deliveryId, AdminDeliveryDecisionRequest request) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        deliveryValidator.validateAdminDecisionRequest(delivery, request);

        deliveryAdminDecisionHandler.applyDecision(delivery, request);

        if (deliveryAdminDecisionHandler.isPayrollEligible(request.decision())) {
            markMandorPayrollQueued(delivery);
        }

        OffsetDateTime currentTime = now();
        delivery.setAdminDecision(request.decision());
        delivery.setAdminDecidedAt(currentTime);
        delivery.setUpdatedAt(currentTime);

        return toResponse(deliveryRepository.save(delivery));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getByDriver(Long driverId) {
        return toResponseList(deliveryRepository.findByDriver(driverId));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getByMandor(Long mandorId) {
        return toResponseList(deliveryRepository.findByMandor(mandorId));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getMandorOngoing(
            Long mandorId,
            Long driverId,
            LocalDate date,
            String keyword
    ) {
        return toResponseList(
                deliveryRepository.findMandorDeliveries(
                        mandorId,
                        ONGOING_STATUSES,
                        driverId,
                        date,
                        keyword
                )
        );
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getMandorHistory(
            Long mandorId,
            Long driverId,
            LocalDate date,
            String keyword
    ) {
        return toResponseList(
                deliveryRepository.findMandorDeliveries(
                        mandorId,
                        HISTORY_STATUSES,
                        driverId,
                        date,
                        keyword
                )
        );
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getMandorDeliveryDetail(Long mandorId, Long deliveryId) {
        Delivery delivery = getMandorDeliveryOrThrow(deliveryId, mandorId);
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDriverAssigned(Long driverId) {
        return toResponseList(
                deliveryRepository.findDriverDeliveriesByStatuses(driverId, ONGOING_STATUSES)
        );
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDriverHistory(Long driverId) {
        return toResponseList(
                deliveryRepository.findDriverDeliveriesByStatuses(driverId, HISTORY_STATUSES)
        );
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDriverDeliveryDetail(Long driverId, Long deliveryId) {
        Delivery delivery = deliveryRepository.findByIdAndDriver_Id(deliveryId, driverId)
            .orElseThrow(() -> new IllegalArgumentException(DRIVER_DELIVERY_NOT_FOUND));
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getAdminReadyDeliveries(String mandorName, LocalDate date) {
        return toResponseList(deliveryRepository.findAdminReadyDeliveries(mandorName, date));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryDetail(Long deliveryId) {
        return toResponse(getDeliveryOrThrow(deliveryId));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getForAdminReview() {
        return toResponseList(
                deliveryRepository.findForAdminReview(Delivery.MandorDecision.Approved)
        );
    }

    private Delivery getMandorDeliveryOrThrow(Long deliveryId, Long mandorId) {
        return deliveryRepository.findByIdAndMandor_Id(deliveryId, mandorId)
                .orElseThrow(() -> new IllegalArgumentException(MANDOR_DELIVERY_NOT_FOUND));
    }

    private void requireCreateRequest(CreateDeliveryRequest request) {
        if (request == null || request.driverId() == null || request.hasilPanenId() == null) {
            throw new IllegalArgumentException("driverId and hasilPanenId are required");
        }
    }

    private User getUserOrThrow(Long userId, String notFoundMessage) {
        return Optional.ofNullable(entityManager.find(User.class, userId))
                .orElseThrow(() -> new IllegalArgumentException(notFoundMessage));
    }

    private HasilPanen getHasilPanenOrThrow(Long hasilPanenId) {
        return Optional.ofNullable(entityManager.find(HasilPanen.class, hasilPanenId))
                .orElseThrow(() -> new IllegalArgumentException(HARVEST_NOT_FOUND));
    }

    private void markDriverPayrollQueued(Delivery delivery) {
        delivery.setDriverPayrollTriggerState(Delivery.PayrollTriggerState.Queued);
        delivery.setDriverPayrollTriggeredAt(now());
    }

    private void markMandorPayrollQueued(Delivery delivery) {
        delivery.setMandorPayrollTriggerState(Delivery.PayrollTriggerState.Queued);
        delivery.setMandorPayrollTriggeredAt(now());
    }

    private Delivery getDeliveryOrThrow(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException(DELIVERY_NOT_FOUND));
    }

    private OffsetDateTime now() {
        return timeProvider.now();
    }

    private DeliveryResponse toResponse(Delivery delivery) {
        return deliveryMapper.toResponse(delivery);
    }

    private List<DeliveryResponse> toResponseList(List<Delivery> deliveries) {
        return deliveryMapper.toResponseList(deliveries);
    }
}
