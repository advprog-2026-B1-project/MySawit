package com.b1.mysawit.repository;

import com.b1.mysawit.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findAllByDriver_IdOrderByUpdatedAtDesc(Long driverId);

    List<Delivery> findAllByMandor_IdOrderByUpdatedAtDesc(Long mandorId);

    List<Delivery> findAllByMandorDecisionOrderByMandorDecidedAtDesc(
            Delivery.MandorDecision mandorDecision
    );

    Optional<Delivery> findByIdAndMandor_Id(Long id, Long mandorId);

    Optional<Delivery> findByIdAndDriver_Id(Long id, Long driverId);

    boolean existsByHasilPanen_IdAndStatusIn(Long hasilPanenId, Collection<Delivery.Status> statuses);

    @Query("""
            SELECT COALESCE(SUM(d.hasilPanen.kilogram), 0)
            FROM Delivery d
            WHERE d.driver.id = :driverId
              AND d.status IN :ongoingStatuses
            """)
    BigDecimal sumOngoingKgByDriver(
            @Param("driverId") Long driverId,
            @Param("ongoingStatuses") Collection<Delivery.Status> ongoingStatuses
    );

    @Query("""
            SELECT d
            FROM Delivery d
            WHERE d.mandor.id = :mandorId
              AND d.status IN :statuses
              AND (:driverId IS NULL OR d.driver.id = :driverId)
              AND (:date IS NULL OR FUNCTION('date', d.createdAt) = :date)
              AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(d.driver.nama) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(d.driver.username) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY d.updatedAt DESC
            """)
    List<Delivery> findMandorDeliveries(
            @Param("mandorId") Long mandorId,
            @Param("statuses") Collection<Delivery.Status> statuses,
            @Param("driverId") Long driverId,
            @Param("date") LocalDate date,
            @Param("keyword") String keyword
    );

    @Query("""
            SELECT d
            FROM Delivery d
            WHERE d.driver.id = :driverId
              AND d.status IN :statuses
            ORDER BY d.updatedAt DESC
            """)
    List<Delivery> findDriverDeliveriesByStatuses(
            @Param("driverId") Long driverId,
            @Param("statuses") Collection<Delivery.Status> statuses
    );

    @Query("""
            SELECT d
            FROM Delivery d
            WHERE d.mandorDecision = com.b1.mysawit.domain.Delivery.MandorDecision.Approved
              AND d.adminDecision = com.b1.mysawit.domain.Delivery.AdminDecision.Pending
              AND (:mandorName IS NULL OR :mandorName = ''
               OR LOWER(d.mandor.nama) LIKE LOWER(CONCAT('%', :mandorName, '%')))
              AND (:date IS NULL OR FUNCTION('date', d.mandorDecidedAt) = :date)
            ORDER BY d.mandorDecidedAt DESC
            """)
    List<Delivery> findAdminReadyDeliveries(
            @Param("mandorName") String mandorName,
            @Param("date") LocalDate date
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
