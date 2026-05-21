package com.b1.mysawit.delivery.service;

import com.b1.mysawit.delivery.dto.AdminDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.MandorDeliveryDecisionRequest;
import com.b1.mysawit.domain.Delivery;
import com.b1.mysawit.domain.HasilPanen;
import com.b1.mysawit.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeliveryValidatorTest {

    private DeliveryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DeliveryValidator();
    }

    @Test
    void validateStatusTransition_shouldRejectBackwardTransition() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateStatusTransition(Delivery.Status.Mengirim, Delivery.Status.Memuat)
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Invalid delivery status transition",
                exception.getMessage()
        );
    }

    @Test
    void validateStatusTransition_shouldAllowForwardTransition() {
        assertDoesNotThrow(() -> validator.validateStatusTransition(
                Delivery.Status.Memuat,
                Delivery.Status.Mengirim
        ));
    }

    @Test
    void validateMandorDecisionRequest_shouldRequireRejectionReason() {
        Delivery delivery = sampleDelivery(Delivery.Status.Tiba);

        MandorDeliveryDecisionRequest request = new MandorDeliveryDecisionRequest(
                Delivery.MandorDecision.Rejected,
                ""
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateMandorDecisionRequest(delivery, 20L, request)
        );

        org.junit.jupiter.api.Assertions.assertEquals("Rejection reason is required", exception.getMessage());
    }

    @Test
    void validateAdminDecisionRequest_shouldRequireReasonOnPartialApproval() {
        Delivery delivery = sampleDelivery(Delivery.Status.Tiba);
        delivery.setMandorDecision(Delivery.MandorDecision.Approved);

        AdminDeliveryDecisionRequest request = new AdminDeliveryDecisionRequest(
                Delivery.AdminDecision.PartiallyApproved,
                new BigDecimal("50"),
                ""
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateAdminDecisionRequest(delivery, request)
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Rejection reason is required for partial approval",
                exception.getMessage()
        );
    }

    @Test
    void validateAdminDecisionRequest_shouldRejectAcknowledgedKgGreaterThanDelivered() {
        Delivery delivery = sampleDelivery(Delivery.Status.Tiba);
        delivery.setMandorDecision(Delivery.MandorDecision.Approved);
        delivery.getHasilPanen().setKilogram(new BigDecimal("100"));

        AdminDeliveryDecisionRequest request = new AdminDeliveryDecisionRequest(
                Delivery.AdminDecision.PartiallyApproved,
                new BigDecimal("120"),
                "partial reject"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateAdminDecisionRequest(delivery, request)
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Acknowledged kilogram cannot exceed delivered kilogram",
                exception.getMessage()
        );
    }

    @Test
    void validateCreateDelivery_shouldRejectNonApprovedHarvest() {
        User mandor = new User();
        mandor.setRole(User.Role.Mandor);

        User driver = new User();
        driver.setRole(User.Role.Supir);

        HasilPanen hasilPanen = new HasilPanen();
        hasilPanen.setStatus(HasilPanen.Status.Pending);
        hasilPanen.setKilogram(new BigDecimal("50"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCreateDelivery(mandor, driver, hasilPanen)
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Only approved harvest can be delivered",
                exception.getMessage()
        );
    }

    @Test
    void validateAssignedDriver_shouldRejectDifferentDriver() {
        Delivery delivery = sampleDelivery(Delivery.Status.Memuat);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateAssignedDriver(delivery, 999L)
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Driver is not assigned to this delivery",
                exception.getMessage()
        );
    }

    @Test
    void validateMandorDecisionRequest_shouldRejectIfDecisionAlreadySubmitted() {
        Delivery delivery = sampleDelivery(Delivery.Status.Tiba);
        delivery.setMandorDecision(Delivery.MandorDecision.Approved);

        MandorDeliveryDecisionRequest request = new MandorDeliveryDecisionRequest(
                Delivery.MandorDecision.Rejected,
                "late reject"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateMandorDecisionRequest(delivery, 20L, request)
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Mandor decision has already been submitted",
                exception.getMessage()
        );
    }

    @Test
    void validateAdminDecisionRequest_shouldRejectIfDecisionAlreadySubmitted() {
        Delivery delivery = sampleDelivery(Delivery.Status.Tiba);
        delivery.setMandorDecision(Delivery.MandorDecision.Approved);
        delivery.setAdminDecision(Delivery.AdminDecision.Approved);

        AdminDeliveryDecisionRequest request = new AdminDeliveryDecisionRequest(
                Delivery.AdminDecision.Rejected,
                BigDecimal.ZERO,
                "already decided"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateAdminDecisionRequest(delivery, request)
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Admin decision has already been submitted",
                exception.getMessage()
        );
    }

    private Delivery sampleDelivery(Delivery.Status status) {
        User driver = new User();
        driver.setId(10L);
        driver.setRole(User.Role.Supir);

        User mandor = new User();
        mandor.setId(20L);
        mandor.setRole(User.Role.Mandor);

        HasilPanen hasilPanen = new HasilPanen();
        hasilPanen.setKilogram(new BigDecimal("80"));

        Delivery delivery = new Delivery();
        delivery.setDriver(driver);
        delivery.setMandor(mandor);
        delivery.setHasilPanen(hasilPanen);
        delivery.setStatus(status);
        delivery.setMandorDecision(Delivery.MandorDecision.Pending);
        delivery.setAdminDecision(Delivery.AdminDecision.Pending);
        return delivery;
    }
}
