package com.b1.mysawit.delivery.service;

import com.b1.mysawit.delivery.dto.AdminDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.MandorDeliveryDecisionRequest;
import com.b1.mysawit.delivery.dto.UpdateDeliveryStatusRequest;
import com.b1.mysawit.domain.Delivery;
import org.springframework.stereotype.Component;

@Component
public class DeliveryValidator {

	public void validateAssignedDriver(Delivery delivery, Long driverId) {
		if (delivery.getDriver() == null || !delivery.getDriver().getId().equals(driverId)) {
			throw new IllegalArgumentException("Driver is not assigned to this delivery");
		}
	}

	public Delivery.Status requireNextStatus(UpdateDeliveryStatusRequest request) {
		if (request == null || request.status() == null) {
			throw new IllegalArgumentException("Status is required");
		}
		return request.status();
	}

	public void validateStatusTransition(Delivery.Status current, Delivery.Status next) {
		boolean isValidForwardTransition =
				(current == Delivery.Status.Memuat && next == Delivery.Status.Mengirim)
						|| (current == Delivery.Status.Mengirim && next == Delivery.Status.Tiba);

		if (!isValidForwardTransition) {
			throw new IllegalArgumentException("Invalid delivery status transition");
		}
	}

	public void validateMandorDecisionRequest(
			Delivery delivery,
			Long mandorId,
			MandorDeliveryDecisionRequest request
	) {
		if (delivery.getMandor() == null || !delivery.getMandor().getId().equals(mandorId)) {
			throw new IllegalArgumentException("Mandor is not assigned to this delivery");
		}

		if (delivery.getStatus() != Delivery.Status.Tiba) {
			throw new IllegalArgumentException("Mandor decision is allowed only after delivery reaches Tiba");
		}

		if (request == null || request.decision() == null || request.decision() == Delivery.MandorDecision.Pending) {
			throw new IllegalArgumentException("Mandor decision is required");
		}

		boolean missingReason = request.rejectionReason() == null || request.rejectionReason().isBlank();
		if (request.decision() == Delivery.MandorDecision.Rejected && missingReason) {
			throw new IllegalArgumentException("Rejection reason is required");
		}
	}

	public void validateAdminDecisionRequest(Delivery delivery, AdminDeliveryDecisionRequest request) {
		if (delivery.getMandorDecision() != Delivery.MandorDecision.Approved) {
			throw new IllegalArgumentException("Admin decision is allowed only after mandor approval");
		}

		if (request == null || request.decision() == null || request.decision() == Delivery.AdminDecision.Pending) {
			throw new IllegalArgumentException("Admin decision is required");
		}

		boolean missingReason = request.rejectionReason() == null || request.rejectionReason().isBlank();
		if (request.decision() == Delivery.AdminDecision.Rejected && missingReason) {
			throw new IllegalArgumentException("Admin rejection reason is required");
		}

		if (request.decision() == Delivery.AdminDecision.PartiallyApproved) {
			validatePartialApproval(delivery, request);
		}
	}

	private void validatePartialApproval(Delivery delivery, AdminDeliveryDecisionRequest request) {
		if (request.acknowledgedKg() == null || request.acknowledgedKg().signum() <= 0) {
			throw new IllegalArgumentException("Acknowledged kilogram is required for partial approval");
		}

		if (delivery.getHasilPanen() == null || delivery.getHasilPanen().getKilogram() == null) {
			throw new IllegalArgumentException("Delivery harvest kilogram is missing");
		}

		if (request.acknowledgedKg().compareTo(delivery.getHasilPanen().getKilogram()) > 0) {
			throw new IllegalArgumentException("Acknowledged kilogram cannot exceed delivered kilogram");
		}

		if (request.rejectionReason() == null || request.rejectionReason().isBlank()) {
			throw new IllegalArgumentException("Rejection reason is required for partial approval");
		}
	}
}
