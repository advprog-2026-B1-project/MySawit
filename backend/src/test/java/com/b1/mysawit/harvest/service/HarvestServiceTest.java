package com.b1.mysawit.harvest.service;

import com.b1.mysawit.domain.FotoHasilPanen;
import com.b1.mysawit.domain.HasilPanen;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.harvest.dto.HarvestRequest;
import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.repository.FotoHasilPanenRepository;
import com.b1.mysawit.repository.HasilPanenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestServiceTest {

    @Mock
    private HasilPanenRepository hasilPanenRepository;

    @Mock
    private FotoHasilPanenRepository fotoHasilPanenRepository;  // add this

    @Mock
    private SupabaseService supabaseService;

    @InjectMocks
    private HarvestService harvestService;

    private User dummyUser;
    private HasilPanen dummyPanen;

    @BeforeEach
    void setUp() {
        dummyUser = new User();
        dummyUser.setId(1L);

        dummyPanen = new HasilPanen();
        dummyPanen.setId(100L);
        dummyPanen.setWorker(dummyUser);
        dummyPanen.setStatus(HasilPanen.Status.Pending);
        dummyPanen.setTanggalPanen(LocalDate.now());
    }

    @Test
    void createHarvest_ShouldThrowException_WhenBuruhAlreadyInputToday() {
        User worker = new User();
        worker.setId(1L);
        HarvestRequest request = new HarvestRequest();
        request.setKilogram(new BigDecimal("150"));

        when(hasilPanenRepository.existsByWorker_IdAndTanggalPanen(eq(1L), any(LocalDate.class)))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            harvestService.createHarvest(worker, request);
        });
        verify(hasilPanenRepository, never()).save(any());
    }

    @Test
    void testCreateHarvest_WithPhotos_Success() {
        // Arrange
        HarvestRequest request = new HarvestRequest();
        request.setKilogram(new BigDecimal("150.5"));
        request.setBerita("Blok A selesai");

        MockMultipartFile file1 = new MockMultipartFile("photos", "foto1.jpg", "image/jpeg", "image_data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("photos", "foto2.jpg", "image/jpeg", "image_data".getBytes());
        request.setPhotos(List.of(file1, file2));

        when(hasilPanenRepository.existsByWorker_IdAndTanggalPanen(1L, LocalDate.now())).thenReturn(false);
        when(hasilPanenRepository.save(any(HasilPanen.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(supabaseService.uploadPhoto(any(MultipartFile.class)))
                .thenReturn("https://fake-url.com/foto1.jpg");

        when(fotoHasilPanenRepository.save(any(FotoHasilPanen.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        HarvestResponse response = harvestService.createHarvest(dummyUser, request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("150.5"), response.getKilogram());
        verify(hasilPanenRepository, times(1)).save(any(HasilPanen.class));
    }

    @Test
    void testApproveHarvest_Success() {
        // Arrange
        when(hasilPanenRepository.findById(100L)).thenReturn(Optional.of(dummyPanen));
        when(hasilPanenRepository.save(any(HasilPanen.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fotoHasilPanenRepository.findAllByHasilPanen_Id(any()))
                .thenReturn(List.of());

        // Act
        HarvestResponse response = harvestService.approveHarvest(100L);

        // Assert
        assertEquals("Approved", response.getStatus()); // Sesuaikan string balikan dengan Enum name()
        verify(hasilPanenRepository, times(1)).save(dummyPanen);
    }

    @Test
    void testApproveHarvest_Fail_NotFound() {
        // Arrange
        Long targetId = 100L;
        when(hasilPanenRepository.findById(targetId)).thenReturn(Optional.empty());

        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            harvestService.approveHarvest(targetId);
        });

        verify(hasilPanenRepository, never()).save(any());
    }

    @Test
    void testRejectHarvest_Success() {
        // Arrange
        when(hasilPanenRepository.findById(100L)).thenReturn(Optional.of(dummyPanen));
        when(hasilPanenRepository.save(any(HasilPanen.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        HarvestResponse response = harvestService.rejectHarvest(100L, "Kualitas buah buruk");

        // Assert
        assertEquals("Rejected", response.getStatus());
        verify(hasilPanenRepository, times(1)).save(dummyPanen);
    }

    @Test
    void testRejectHarvest_ShouldThrowException_WhenIdIsNull() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> harvestService.rejectHarvest(null, "alasan")
        );

        assertEquals("Data panen tidak ditemukan", ex.getMessage());
    }

    @Test
    void getMyHarvestHistory_shouldReturnList() {

        User user = new User();
        user.setId(1L);

        HasilPanen panen = new HasilPanen();
        panen.setId(10L);
        panen.setTanggalPanen(LocalDate.now());
        panen.setKilogram(BigDecimal.valueOf(100));
        panen.setBerita("test");
        panen.setStatus(HasilPanen.Status.Pending);

        when(hasilPanenRepository.findAllByWorker_IdOrderByTanggalPanenDesc(1L))
                .thenReturn(List.of(panen));

        when(fotoHasilPanenRepository.findAllByHasilPanen_Id(10L))
                .thenReturn(List.of());

        List<HarvestResponse> result = harvestService.getMyHarvestHistory(user);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void createHarvest_shouldThrowException_whenPhotosIsEmpty() {

        User user = new User();

        HarvestRequest request = mock(HarvestRequest.class);
        when(request.getPhotos()).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> harvestService.createHarvest(user, request)
        );

        assertTrue(ex.getMessage().contains("Minimal 1 foto"));
    }

    @Test
    void createHarvest_shouldThrowException_whenPhotosIsNull() {
        // Arrange
        User user = new User();
        user.setId(1L);

        HarvestRequest request = mock(HarvestRequest.class);
        when(request.getPhotos()).thenReturn(null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> harvestService.createHarvest(user, request)
        );

        assertTrue(ex.getMessage().contains("Minimal 1 foto"));
        verify(hasilPanenRepository, never()).save(any());
    }

}