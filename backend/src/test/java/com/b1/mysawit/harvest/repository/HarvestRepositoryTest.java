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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class HarvestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HasilPanenRepository repository;

    // -------------------------------------------------------------------------
    // existsByWorker_IdAndTanggalPanen
    // -------------------------------------------------------------------------

    @Test
    void existsByWorker_IdAndTanggalPanen_ReturnsTrue_WhenDataExists() {
        User worker = entityManager.persist(new User());

        HasilPanen panen = new HasilPanen();
        panen.setWorker(worker);
        panen.setTanggalPanen(LocalDate.now());
        panen.setKilogram(new BigDecimal("100"));
        panen.setStatus(HasilPanen.Status.Pending);
        entityManager.persist(panen);
        entityManager.flush();

        assertTrue(repository.existsByWorker_IdAndTanggalPanen(worker.getId(), LocalDate.now()));
    }

    @Test
    void existsByWorker_IdAndTanggalPanen_ReturnsFalse_WhenNoData() {
        assertFalse(repository.existsByWorker_IdAndTanggalPanen(999L, LocalDate.now()));
    }

    @Test
    void existsByWorker_IdAndTanggalPanen_ReturnsFalse_WhenDifferentDate() {
        User worker = entityManager.persist(new User());

        HasilPanen panen = new HasilPanen();
        panen.setWorker(worker);
        panen.setTanggalPanen(LocalDate.now().minusDays(1)); // yesterday
        panen.setKilogram(new BigDecimal("100"));
        panen.setStatus(HasilPanen.Status.Pending);
        entityManager.persist(panen);
        entityManager.flush();

        assertFalse(repository.existsByWorker_IdAndTanggalPanen(worker.getId(), LocalDate.now()));
    }

    @Test
    void existsByWorker_IdAndTanggalPanen_ReturnsFalse_WhenDifferentWorker() {
        User worker1 = entityManager.persist(new User());
        User worker2 = entityManager.persist(new User());

        HasilPanen panen = new HasilPanen();
        panen.setWorker(worker1);
        panen.setTanggalPanen(LocalDate.now());
        panen.setKilogram(new BigDecimal("100"));
        panen.setStatus(HasilPanen.Status.Pending);
        entityManager.persist(panen);
        entityManager.flush();

        // worker2 has no record today
        assertFalse(repository.existsByWorker_IdAndTanggalPanen(worker2.getId(), LocalDate.now()));
    }

    // -------------------------------------------------------------------------
    // findAllByWorker_IdOrderByTanggalPanenDesc
    // -------------------------------------------------------------------------

    @Test
    void findAllByWorker_IdOrderByTanggalPanenDesc_ReturnsDescendingOrder() {
        User worker = entityManager.persist(new User());

        for (int i = 1; i <= 3; i++) {
            HasilPanen p = new HasilPanen();
            p.setWorker(worker);
            p.setTanggalPanen(LocalDate.now().minusDays(i));
            p.setKilogram(new BigDecimal("100"));
            p.setStatus(HasilPanen.Status.Pending);
            entityManager.persist(p);
        }
        entityManager.flush();

        List<HasilPanen> result = repository.findAllByWorker_IdOrderByTanggalPanenDesc(worker.getId());

        assertEquals(3, result.size());
        assertTrue(result.get(0).getTanggalPanen().isAfter(result.get(1).getTanggalPanen()));
        assertTrue(result.get(1).getTanggalPanen().isAfter(result.get(2).getTanggalPanen()));
    }

    @Test
    void findAllByWorker_IdOrderByTanggalPanenDesc_ReturnsEmpty_WhenNoData() {
        List<HasilPanen> result = repository.findAllByWorker_IdOrderByTanggalPanenDesc(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByWorker_IdOrderByTanggalPanenDesc_ReturnsOnlyOwnRecords() {
        User worker1 = entityManager.persist(new User());
        User worker2 = entityManager.persist(new User());

        HasilPanen p1 = new HasilPanen();
        p1.setWorker(worker1);
        p1.setTanggalPanen(LocalDate.now());
        p1.setKilogram(new BigDecimal("100"));
        p1.setStatus(HasilPanen.Status.Pending);
        entityManager.persist(p1);

        HasilPanen p2 = new HasilPanen();
        p2.setWorker(worker2);
        p2.setTanggalPanen(LocalDate.now());
        p2.setKilogram(new BigDecimal("200"));
        p2.setStatus(HasilPanen.Status.Pending);
        entityManager.persist(p2);

        entityManager.flush();

        List<HasilPanen> result = repository.findAllByWorker_IdOrderByTanggalPanenDesc(worker1.getId());

        assertEquals(1, result.size());
        assertEquals(worker1.getId(), result.get(0).getWorker().getId());
    }
}