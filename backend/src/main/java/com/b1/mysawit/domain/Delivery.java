package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne
    @JoinColumn(name = "mandor_id")
    private User mandor;

    @ManyToOne
    @JoinColumn(name = "hasil_panen_id")
    private HasilPanen hasilPanen;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.Memuat;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MandorDecision mandorDecision = MandorDecision.Pending;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminDecision adminDecision = AdminDecision.Pending;

    private BigDecimal acknowledgedKg;

    private String mandorRejectionReason;
    private String adminRejectionReason;

    private OffsetDateTime arrivedAt;
    private OffsetDateTime mandorDecidedAt;
    private OffsetDateTime adminDecidedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PayrollTriggerState driverPayrollTriggerState = PayrollTriggerState.NotTriggered;

    private OffsetDateTime driverPayrollTriggeredAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PayrollTriggerState mandorPayrollTriggerState = PayrollTriggerState.NotTriggered;

    private OffsetDateTime mandorPayrollTriggeredAt;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public enum Status {
        Memuat, Mengirim, Tiba
    }

    public enum MandorDecision {
        Pending, Approved, Rejected
    }

    public enum AdminDecision {
        Pending, Approved, Rejected, PartiallyApproved
    }

    public enum PayrollTriggerState {
        NotTriggered, Queued, Triggered, Failed
    }
}