package com.b1.mysawit.harvest.repository;

import com.b1.mysawit.domain.HasilPanen;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.HasilPanenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class HarvestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HasilPanenRepository repository;

    @Test
    void existsByWorker_IdAndTanggalPanen_ReturnsTrue_IfDataExists() {
        User worker = new User();
        worker = entityManager.persist(worker);

        HasilPanen panen = new HasilPanen();
        panen.setWorker(worker);
        panen.setTanggalPanen(LocalDate.now());
        panen.setKilogram(new BigDecimal("100"));
        panen.setStatus(HasilPanen.Status.Pending);
        entityManager.persist(panen);
        entityManager.flush();

        boolean exists = repository.existsByWorker_IdAndTanggalPanen(worker.getId(), LocalDate.now());

        assertTrue(exists);
    }
}