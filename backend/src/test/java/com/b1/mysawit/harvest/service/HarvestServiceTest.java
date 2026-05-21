package com.b1.mysawit.harvest.service;

import com.b1.mysawit.domain.HasilPanen;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.domain.WorkerAssignment;
import com.b1.mysawit.harvest.dto.HarvestRequest;
import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.repository.FotoHasilPanenRepository;
import com.b1.mysawit.repository.HasilPanenRepository;
import com.b1.mysawit.repository.WorkerAssignmentRepository;
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
    private FotoHasilPanenRepository fotoHasilPanenRepository;

    @Mock
    private SupabaseService supabaseService;

    @Mock
    private WorkerAssignmentRepository workerAssignmentRepository;

    @InjectMocks
    private HarvestService harvestService;

    private User dummyWorker;
    private User dummyMandor;
    private HasilPanen dummyPanen;

    @BeforeEach
    void setUp() {
        dummyWorker = new User();
        dummyWorker.setId(1L);
        dummyWorker.setRole(User.Role.Buruh);
        dummyWorker.setNama("Budi");

        dummyMandor = new User();
        dummyMandor.setId(2L);
        dummyMandor.setRole(User.Role.Mandor);

        dummyPanen = new HasilPanen();
        dummyPanen.setId(100L);
        dummyPanen.setWorker(dummyWorker);
        dummyPanen.setStatus(HasilPanen.Status.Pending);
        dummyPanen.setTanggalPanen(LocalDate.now());
    }

    private void mockWorkerAssignment() {
        WorkerAssignment assignment = new WorkerAssignment();
        assignment.setWorker(dummyWorker);
        assignment.setMandor(dummyMandor);
        when(workerAssignmentRepository.findByWorkerIdAndUnassignedAtIsNull(dummyWorker.getId()))
                .thenReturn(Optional.of(assignment));
    }

    @Test
    void validateMandorAuthorization_ThrowsException_WhenUserIsNotMandor() {
        User notMandor = new User();
        notMandor.setRole(User.Role.Buruh);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            harvestService.approveHarvest(100L, notMandor);
        });
        assertTrue(ex.getMessage().contains("Hanya Mandor"));
    }

    @Test
    void validateMandorAuthorization_ThrowsException_WhenMandorIdDoesNotMatch() {
        when(hasilPanenRepository.findById(100L)).thenReturn(Optional.of(dummyPanen));
        mockWorkerAssignment();

        User wrongMandor = new User();
        wrongMandor.setId(99L);
        wrongMandor.setRole(User.Role.Mandor);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            harvestService.approveHarvest(100L, wrongMandor);
        });
        assertTrue(ex.getMessage().contains("tidak memiliki akses"));
    }

    @Test
    void getMyHarvestHistory_WithValidStatus_ParsesStatusEnum() {
        when(hasilPanenRepository.findByWorkerIdWithFilters(eq(dummyWorker.getId()), isNull(), isNull(), eq(HasilPanen.Status.Pending)))
                .thenReturn(List.of(dummyPanen));
        when(fotoHasilPanenRepository.findAllByHasilPanen_Id(100L)).thenReturn(List.of());

        List<HarvestResponse> result = harvestService.getMyHarvestHistory(dummyWorker, null, null, "Pending");
        assertEquals(1, result.size());
    }

    @Test
    void getMandorHarvestHistory_ThrowsException_WhenNotMandor() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            harvestService.getMandorHarvestHistory(dummyWorker, null, null, null, null); // dummyWorker is Buruh
        });
        assertTrue(ex.getMessage().contains("Hanya Mandor"));
    }

    @Test
    void getMandorHarvestHistory_SuccessWithStatusFilter() {
        when(hasilPanenRepository.findForMandorWithFilters(eq(dummyMandor.getId()), isNull(), isNull(), eq(HasilPanen.Status.Approved), eq("Budi")))
                .thenReturn(List.of(dummyPanen));
        when(fotoHasilPanenRepository.findAllByHasilPanen_Id(100L)).thenReturn(List.of());

        List<HarvestResponse> result = harvestService.getMandorHarvestHistory(dummyMandor, null, null, "Approved", "Budi");
        assertEquals(1, result.size());
    }

    @Test
    void createHarvest_ShouldThrowException_WhenBuruhAlreadyInputToday() {
        HarvestRequest request = new HarvestRequest();
        request.setKilogram(new BigDecimal("150"));
        MockMultipartFile dummyFile = new MockMultipartFile("photos", "test.jpg", "image/jpeg", "image".getBytes());
        request.setPhotos(List.of(dummyFile));

        when(hasilPanenRepository.existsByWorker_IdAndTanggalPanen(eq(1L), any(LocalDate.class))).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> harvestService.createHarvest(dummyWorker, request));
    }

    @Test
    void testCreateHarvest_WithPhotos_Success() {
        HarvestRequest request = new HarvestRequest();
        request.setKilogram(new BigDecimal("150.5"));
        request.setPhotos(List.of(new MockMultipartFile("photos", "foto1.jpg", "image/jpeg", "image_data".getBytes())));

        when(hasilPanenRepository.existsByWorker_IdAndTanggalPanen(1L, LocalDate.now())).thenReturn(false);
        when(hasilPanenRepository.save(any(HasilPanen.class))).thenAnswer(i -> i.getArgument(0));
        when(supabaseService.uploadPhoto(any(MultipartFile.class))).thenReturn("url");

        HarvestResponse response = harvestService.createHarvest(dummyWorker, request);
        assertNotNull(response);
    }

    @Test
    void testApproveHarvest_Success() {
        when(hasilPanenRepository.findById(100L)).thenReturn(Optional.of(dummyPanen));
        mockWorkerAssignment();
        when(hasilPanenRepository.save(any(HasilPanen.class))).thenAnswer(i -> i.getArgument(0));

        HarvestResponse response = harvestService.approveHarvest(100L, dummyMandor);
        assertEquals("Approved", response.getStatus());
    }

    @Test
    void testApproveHarvest_Fail_NotFound() {
        when(hasilPanenRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> harvestService.approveHarvest(100L, dummyMandor));
    }

    @Test
    void testRejectHarvest_Success() {
        when(hasilPanenRepository.findById(100L)).thenReturn(Optional.of(dummyPanen));
        mockWorkerAssignment();
        when(hasilPanenRepository.save(any(HasilPanen.class))).thenAnswer(i -> i.getArgument(0));

        HarvestResponse response = harvestService.rejectHarvest(100L, "Kualitas buruk", dummyMandor);
        assertEquals("Rejected", response.getStatus());
    }

    @Test
    void testRejectHarvest_ShouldThrowException_WhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> harvestService.rejectHarvest(null, "alasan", dummyMandor));
    }

    @Test
    void getMyHarvestHistory_shouldReturnList() {
        when(hasilPanenRepository.findByWorkerIdWithFilters(eq(1L), isNull(), isNull(), isNull())).thenReturn(List.of(dummyPanen));
        List<HarvestResponse> result = harvestService.getMyHarvestHistory(dummyWorker, null, null, null);
        assertEquals(1, result.size());
    }

    @Test
    void createHarvest_shouldThrowException_whenPhotosIsEmpty() {
        HarvestRequest request = mock(HarvestRequest.class);
        when(request.getPhotos()).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> harvestService.createHarvest(dummyWorker, request));
    }

    @Test
    void createHarvest_shouldThrowException_whenPhotosIsNull() {
        HarvestRequest request = mock(HarvestRequest.class);
        when(request.getPhotos()).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> harvestService.createHarvest(dummyWorker, request));
    }
}