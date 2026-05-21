package com.b1.mysawit.delivery.service;

import com.b1.mysawit.domain.Delivery;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.DeliveryRepository;
import com.b1.mysawit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FakePayrollProcessor {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void processQueuedPayrolls() {
        log.info("Running FakePayrollProcessor...");
        processDriverPayrolls();
        processMandorPayrolls();
    }

    private void processDriverPayrolls() {
        List<Delivery> driverQueued = deliveryRepository.findAllByDriverPayrollTriggerState(Delivery.PayrollTriggerState.Queued);
        for (Delivery delivery : driverQueued) {
            try {
                User driver = delivery.getDriver();
                if (driver != null && delivery.getHasilPanen() != null && delivery.getHasilPanen().getKilogram() != null) {
                    BigDecimal kg = delivery.getHasilPanen().getKilogram();
                    BigDecimal pay = kg.multiply(BigDecimal.valueOf(5.0)).multiply(BigDecimal.valueOf(0.90));
                    
                    driver.setSaldo(driver.getSaldo().add(pay));
                    userRepository.save(driver);

                    delivery.setDriverPayrollTriggerState(Delivery.PayrollTriggerState.Triggered);
                    delivery.setDriverPayrollTriggeredAt(OffsetDateTime.now());
                    deliveryRepository.save(delivery);
                    
                    log.info("Processed Driver payroll for Delivery ID: {}, Driver: {}, Amount: {}", delivery.getId(), driver.getUsername(), pay);
                }
            } catch (Exception e) {
                log.error("Failed to process Driver payroll for Delivery ID: {}", delivery.getId(), e);
            }
        }
    }

    private void processMandorPayrolls() {
        List<Delivery> mandorQueued = deliveryRepository.findAllByMandorPayrollTriggerState(Delivery.PayrollTriggerState.Queued);
        for (Delivery delivery : mandorQueued) {
            try {
                User mandor = delivery.getMandor();
                if (mandor != null && delivery.getAcknowledgedKg() != null) {
                    BigDecimal kg = delivery.getAcknowledgedKg();
                    BigDecimal pay = kg.multiply(BigDecimal.valueOf(10.0)).multiply(BigDecimal.valueOf(0.90));

                    mandor.setSaldo(mandor.getSaldo().add(pay));
                    userRepository.save(mandor);

                    delivery.setMandorPayrollTriggerState(Delivery.PayrollTriggerState.Triggered);
                    delivery.setMandorPayrollTriggeredAt(OffsetDateTime.now());
                    deliveryRepository.save(delivery);
                    
                    log.info("Processed Mandor payroll for Delivery ID: {}, Mandor: {}, Amount: {}", delivery.getId(), mandor.getUsername(), pay);
                }
            } catch (Exception e) {
                log.error("Failed to process Mandor payroll for Delivery ID: {}", delivery.getId(), e);
            }
        }
    }
}
