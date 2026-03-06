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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private EntityManager entityManager;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(
                deliveryRepository,
                entityManager,
                new DeliveryMapper(),
                new DeliveryValidator(),
            new DeliveryConstructor(),
            new DriverTripLoadValidator(deliveryRepository),
            new DeliveryAdminDecisionHandler(),
            new SystemTimeProvider()
        );
    }

    @Test
    void createDelivery_shouldCreateWhenRequestIsValid() {
        User mandor = user(1L, User.Role.Mandor);
        User driver = user(2L, User.Role.Supir);

        HasilPanen hasilPanen = new HasilPanen();
        hasilPanen.setId(100L);
        hasilPanen.setStatus(HasilPanen.Status.Approved);
        hasilPanen.setKilogram(new BigDecimal("150"));

        when(entityManager.find(User.class, 1L)).thenReturn(mandor);
        when(entityManager.find(User.class, 2L)).thenReturn(driver);
        when(entityManager.find(HasilPanen.class, 100L)).thenReturn(hasilPanen);
        when(deliveryRepository.existsByHasilPanen_IdAndStatusIn(eq(100L), any())).thenReturn(false);
        when(deliveryRepository.sumOngoingKgByDriver(eq(2L), any())).thenReturn(new BigDecimal("200"));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery saved = invocation.getArgument(0);
            saved.setId(999L);
            return saved;
        });

        DeliveryResponse response = deliveryService.createDelivery(1L, new CreateDeliveryRequest(2L, 100L));

        assertNotNull(response);
        assertEquals(999L, response.id());
        assertEquals(Delivery.Status.Memuat, response.status());
        assertEquals(2L, response.driverId());
        assertEquals(1L, response.mandorId());
    }

    @Test
    void createDelivery_shouldRejectWhenTripTotalExceeds400Kg() {
        User mandor = user(1L, User.Role.Mandor);
        User driver = user(2L, User.Role.Supir);

        HasilPanen hasilPanen = new HasilPanen();
        hasilPanen.setId(100L);
        hasilPanen.setStatus(HasilPanen.Status.Approved);
        hasilPanen.setKilogram(new BigDecimal("100"));

        when(entityManager.find(User.class, 1L)).thenReturn(mandor);
        when(entityManager.find(User.class, 2L)).thenReturn(driver);
        when(entityManager.find(HasilPanen.class, 100L)).thenReturn(hasilPanen);
        when(deliveryRepository.existsByHasilPanen_IdAndStatusIn(eq(100L), any())).thenReturn(false);
        when(deliveryRepository.sumOngoingKgByDriver(eq(2L), any())).thenReturn(new BigDecimal("350"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.createDelivery(1L, new CreateDeliveryRequest(2L, 100L))
        );

        assertEquals("Total delivery load for one trip cannot exceed 400kg", exception.getMessage());
    }

    @Test
    void createDelivery_shouldRejectWhenHarvestAlreadyAssigned() {
        User mandor = user(1L, User.Role.Mandor);
        User driver = user(2L, User.Role.Supir);

        HasilPanen hasilPanen = new HasilPanen();
        hasilPanen.setId(100L);
        hasilPanen.setStatus(HasilPanen.Status.Approved);
        hasilPanen.setKilogram(new BigDecimal("100"));

        when(entityManager.find(User.class, 1L)).thenReturn(mandor);
        when(entityManager.find(User.class, 2L)).thenReturn(driver);
        when(entityManager.find(HasilPanen.class, 100L)).thenReturn(hasilPanen);
        when(deliveryRepository.existsByHasilPanen_IdAndStatusIn(eq(100L), any())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.createDelivery(1L, new CreateDeliveryRequest(2L, 100L))
        );

        assertEquals("Harvest has already been assigned to a delivery", exception.getMessage());
    }

    @Test
    void createDelivery_shouldRejectWhenMandorNotFound() {
        when(entityManager.find(User.class, 1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.createDelivery(1L, new CreateDeliveryRequest(2L, 100L))
        );

        assertEquals("Mandor not found", exception.getMessage());
    }

    @Test
    void updateStatus_shouldMoveToTibaAndSetArrivalTime() {
        Delivery delivery = baseDelivery();
        delivery.setStatus(Delivery.Status.Mengirim);

        when(deliveryRepository.findById(10L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.updateStatus(
                2L,
                10L,
                new UpdateDeliveryStatusRequest(Delivery.Status.Tiba)
        );

        assertEquals(Delivery.Status.Tiba, response.status());
        assertNotNull(response.arrivedAt());
    }

    @Test
    void updateStatus_shouldRejectInvalidTransition() {
        Delivery delivery = baseDelivery();
        delivery.setStatus(Delivery.Status.Memuat);

        when(deliveryRepository.findById(10L)).thenReturn(Optional.of(delivery));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.updateStatus(2L, 10L, new UpdateDeliveryStatusRequest(Delivery.Status.Tiba))
        );

        assertEquals("Invalid delivery status transition", exception.getMessage());
    }

    @Test
    void updateStatus_shouldRejectWhenDeliveryNotFound() {
        when(deliveryRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.updateStatus(2L, 10L, new UpdateDeliveryStatusRequest(Delivery.Status.Mengirim))
        );

        assertEquals("Delivery not found", exception.getMessage());
    }

    @Test
    void decideByMandor_shouldQueueDriverPayrollOnApprove() {
        Delivery delivery = baseDelivery();
        delivery.setStatus(Delivery.Status.Tiba);

        when(deliveryRepository.findByIdAndMandor_Id(10L, 1L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.decideByMandor(
                1L,
                10L,
                new MandorDeliveryDecisionRequest(Delivery.MandorDecision.Approved, null)
        );

        assertEquals(Delivery.MandorDecision.Approved, response.mandorDecision());
        assertEquals(Delivery.PayrollTriggerState.Queued, delivery.getDriverPayrollTriggerState());
        assertNotNull(delivery.getDriverPayrollTriggeredAt());
    }

    @Test
    void decideByMandor_shouldRejectWhenDeliveryNotOwnedByMandor() {
        when(deliveryRepository.findByIdAndMandor_Id(10L, 1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.decideByMandor(
                        1L,
                        10L,
                        new MandorDeliveryDecisionRequest(Delivery.MandorDecision.Approved, null)
                )
        );

        assertEquals("Delivery not found for this mandor", exception.getMessage());
    }

    @Test
    void decideByAdmin_partialApprovalShouldQueueMandorPayroll() {
        Delivery delivery = baseDelivery();
        delivery.setStatus(Delivery.Status.Tiba);
        delivery.setMandorDecision(Delivery.MandorDecision.Approved);

        when(deliveryRepository.findById(10L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.decideByAdmin(
                10L,
                new AdminDeliveryDecisionRequest(
                        Delivery.AdminDecision.PartiallyApproved,
                        new BigDecimal("70"),
                        "Partial reject"
                )
        );

        assertEquals(Delivery.AdminDecision.PartiallyApproved, response.adminDecision());
        assertEquals(new BigDecimal("70"), response.acknowledgedKg());
        assertEquals(Delivery.PayrollTriggerState.Queued, delivery.getMandorPayrollTriggerState());
        assertNotNull(delivery.getMandorPayrollTriggeredAt());
    }

    @Test
    void decideByAdmin_approvedShouldUseHarvestKgAndQueueMandorPayroll() {
        Delivery delivery = baseDelivery();
        delivery.setStatus(Delivery.Status.Tiba);
        delivery.setMandorDecision(Delivery.MandorDecision.Approved);
        delivery.getHasilPanen().setKilogram(new BigDecimal("100"));

        when(deliveryRepository.findById(10L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.decideByAdmin(
                10L,
                new AdminDeliveryDecisionRequest(Delivery.AdminDecision.Approved, null, null)
        );

        assertEquals(Delivery.AdminDecision.Approved, response.adminDecision());
        assertEquals(new BigDecimal("100"), response.acknowledgedKg());
        assertEquals(Delivery.PayrollTriggerState.Queued, delivery.getMandorPayrollTriggerState());
    }

    @Test
    void decideByAdmin_rejectedShouldSetZeroAcknowledgedKg() {
        Delivery delivery = baseDelivery();
        delivery.setStatus(Delivery.Status.Tiba);
        delivery.setMandorDecision(Delivery.MandorDecision.Approved);

        when(deliveryRepository.findById(10L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.decideByAdmin(
                10L,
                new AdminDeliveryDecisionRequest(Delivery.AdminDecision.Rejected, BigDecimal.ZERO, "Damaged")
        );

        assertEquals(Delivery.AdminDecision.Rejected, response.adminDecision());
        assertEquals(BigDecimal.ZERO, response.acknowledgedKg());
    }

    @Test
    void decideByAdmin_shouldRejectWhenDeliveryNotFound() {
        when(deliveryRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.decideByAdmin(
                        10L,
                        new AdminDeliveryDecisionRequest(Delivery.AdminDecision.Approved, null, null)
                )
        );

        assertEquals("Delivery not found", exception.getMessage());
    }

    @Test
    void getByDriver_shouldReturnMappedResponses() {
        Delivery delivery = baseDelivery();
        when(deliveryRepository.findByDriver(2L)).thenReturn(List.of(delivery));

        List<DeliveryResponse> responses = deliveryService.getByDriver(2L);

        assertEquals(1, responses.size());
        assertEquals(2L, responses.get(0).driverId());
    }

    @Test
    void getByMandor_shouldReturnMappedResponses() {
        Delivery delivery = baseDelivery();
        when(deliveryRepository.findByMandor(1L)).thenReturn(List.of(delivery));

        List<DeliveryResponse> responses = deliveryService.getByMandor(1L);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).mandorId());
    }

    @Test
    void getMandorOngoing_shouldReturnFilteredResults() {
        Delivery delivery = baseDelivery();
        when(deliveryRepository.findMandorDeliveries(eq(1L), any(), eq(2L), any(), eq("supir")))
                .thenReturn(List.of(delivery));

        List<DeliveryResponse> responses = deliveryService.getMandorOngoing(
                1L,
                2L,
                LocalDate.of(2026, 3, 6),
                "supir"
        );

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).id());
    }

    @Test
    void getMandorHistory_shouldReturnFilteredResults() {
        Delivery delivery = baseDelivery();
        delivery.setStatus(Delivery.Status.Tiba);
        when(deliveryRepository.findMandorDeliveries(eq(1L), any(), eq(null), eq(null), eq(null)))
                .thenReturn(List.of(delivery));

        List<DeliveryResponse> responses = deliveryService.getMandorHistory(1L, null, null, null);

        assertEquals(1, responses.size());
        assertEquals(Delivery.Status.Tiba, responses.get(0).status());
    }

    @Test
    void getMandorDeliveryDetail_shouldReturnDelivery() {
        Delivery delivery = baseDelivery();
        when(deliveryRepository.findByIdAndMandor_Id(10L, 1L)).thenReturn(Optional.of(delivery));

        DeliveryResponse response = deliveryService.getMandorDeliveryDetail(1L, 10L);

        assertEquals(10L, response.id());
        assertEquals(1L, response.mandorId());
    }

    @Test
    void getMandorDeliveryDetail_shouldRejectWhenNotOwnedByMandor() {
        when(deliveryRepository.findByIdAndMandor_Id(10L, 1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.getMandorDeliveryDetail(1L, 10L)
        );

        assertEquals("Delivery not found for this mandor", exception.getMessage());
    }

    @Test
    void getDriverAssigned_shouldReturnMappedResponses() {
        Delivery delivery = baseDelivery();
        when(deliveryRepository.findDriverDeliveriesByStatuses(eq(2L), any())).thenReturn(List.of(delivery));

        List<DeliveryResponse> responses = deliveryService.getDriverAssigned(2L);

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).id());
        assertEquals(2L, responses.get(0).driverId());
    }

    @Test
    void getDriverDeliveryDetail_shouldRejectWhenNotOwnedByDriver() {
        when(deliveryRepository.findByIdAndDriver_Id(10L, 2L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.getDriverDeliveryDetail(2L, 10L)
        );

        assertEquals("Delivery not found for this driver", exception.getMessage());
    }

    @Test
    void getDriverHistory_shouldReturnMappedResponses() {
        Delivery delivery = baseDelivery();
        delivery.setStatus(Delivery.Status.Tiba);
        when(deliveryRepository.findDriverDeliveriesByStatuses(eq(2L), any())).thenReturn(List.of(delivery));

        List<DeliveryResponse> responses = deliveryService.getDriverHistory(2L);

        assertEquals(1, responses.size());
        assertEquals(Delivery.Status.Tiba, responses.get(0).status());
    }

    @Test
    void getAdminReadyDeliveries_shouldReturnMappedResponses() {
        Delivery delivery = baseDelivery();
        delivery.setStatus(Delivery.Status.Tiba);
        delivery.setMandorDecision(Delivery.MandorDecision.Approved);

        when(deliveryRepository.findAdminReadyDeliveries("mandor", LocalDate.of(2026, 3, 6)))
                .thenReturn(List.of(delivery));

        List<DeliveryResponse> responses = deliveryService.getAdminReadyDeliveries("mandor", LocalDate.of(2026, 3, 6));

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).id());
    }

    @Test
    void getDeliveryDetail_shouldReturnDelivery() {
        Delivery delivery = baseDelivery();
        when(deliveryRepository.findById(10L)).thenReturn(Optional.of(delivery));

        DeliveryResponse response = deliveryService.getDeliveryDetail(10L);

        assertEquals(10L, response.id());
    }

    @Test
    void getDeliveryDetail_shouldRejectWhenNotFound() {
        when(deliveryRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.getDeliveryDetail(10L)
        );

        assertEquals("Delivery not found", exception.getMessage());
    }

    @Test
    void getForAdminReview_shouldReturnMappedResponses() {
        Delivery delivery = baseDelivery();
        delivery.setStatus(Delivery.Status.Tiba);
        delivery.setMandorDecision(Delivery.MandorDecision.Approved);

        when(deliveryRepository.findForAdminReview(Delivery.MandorDecision.Approved))
                .thenReturn(List.of(delivery));

        List<DeliveryResponse> responses = deliveryService.getForAdminReview();

        assertEquals(1, responses.size());
        assertEquals(Delivery.MandorDecision.Approved, responses.get(0).mandorDecision());
    }

    private Delivery baseDelivery() {
        User mandor = user(1L, User.Role.Mandor);
        User driver = user(2L, User.Role.Supir);

        HasilPanen hasilPanen = new HasilPanen();
        hasilPanen.setId(100L);
        hasilPanen.setStatus(HasilPanen.Status.Approved);
        hasilPanen.setKilogram(new BigDecimal("100"));

        Delivery delivery = new Delivery();
        delivery.setId(10L);
        delivery.setMandor(mandor);
        delivery.setDriver(driver);
        delivery.setHasilPanen(hasilPanen);
        delivery.setStatus(Delivery.Status.Memuat);
        delivery.setMandorDecision(Delivery.MandorDecision.Pending);
        delivery.setAdminDecision(Delivery.AdminDecision.Pending);
        return delivery;
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
