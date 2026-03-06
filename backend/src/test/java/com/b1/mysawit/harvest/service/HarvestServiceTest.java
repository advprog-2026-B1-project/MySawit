package com.b1.mysawit.harvest.service;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.harvest.dto.HarvestRequest;
import com.b1.mysawit.repository.HasilPanenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestServiceTest {

    @Mock
    private HasilPanenRepository hasilPanenRepository;

    @InjectMocks
    private HarvestService harvestService;

    @Test
    void createHarvest_ShouldThrowException_WhenBuruhAlreadyInputToday() {
        // Arrange
        User worker = new User();
        worker.setId(1L);
        HarvestRequest request = new HarvestRequest();
        request.setKilogram(new BigDecimal("150"));

        when(hasilPanenRepository.existsByWorker_IdAndTanggalPanen(eq(1L), any(LocalDate.class)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            harvestService.createHarvest(worker, request);
        });
        verify(hasilPanenRepository, never()).save(any());
    }
}