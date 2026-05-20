package com.b1.mysawit.harvest.service;

import com.b1.mysawit.domain.FotoHasilPanen;
import com.b1.mysawit.domain.HasilPanen;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.domain.WorkerAssignment;
import com.b1.mysawit.harvest.dto.HarvestRequest;
import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.repository.FotoHasilPanenRepository;
import com.b1.mysawit.repository.HasilPanenRepository;
import com.b1.mysawit.repository.WorkerAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HarvestService {

    private final HasilPanenRepository hasilPanenRepository;
    private final FotoHasilPanenRepository fotoHasilPanenRepository;
    private final SupabaseService supabaseService;
    private final WorkerAssignmentRepository workerAssignmentRepository;

    private HasilPanen validateMandorAuthorization(Long harvestId, User currentMandor) {
        if (currentMandor.getRole() != User.Role.Mandor) {
            throw new IllegalStateException("Hanya Mandor yang dapat menyetujui/menolak panen");
        }

        HasilPanen panen = hasilPanenRepository.findById(harvestId)
                .orElseThrow(() -> new IllegalArgumentException("Data panen tidak ditemukan"));

        if (currentMandor.getRole() == User.Role.Mandor) {
            WorkerAssignment assignment = workerAssignmentRepository.findByWorkerIdAndUnassignedAtIsNull(panen.getWorker().getId())
                    .orElseThrow(() -> new IllegalStateException("Buruh tidak memiliki mandor yang ditugaskan saat ini"));

            if (!assignment.getMandor().getId().equals(currentMandor.getId())) {
                throw new IllegalStateException("Anda tidak memiliki akses untuk memvalidasi panen buruh ini");
            }
        }
        return panen;
    }

    @Transactional
    public HarvestResponse createHarvest(User currentWorker, HarvestRequest request) {
        LocalDate today = LocalDate.now();

        boolean nonValidPhoto =  request.getPhotos() == null ||
                                 request.getPhotos().isEmpty() ||
                                 request.getPhotos().get(0).isEmpty();

        if (nonValidPhoto) {
            throw new IllegalArgumentException("Minimal 1 foto harus diupload sebagai bukti panen");
        }

        if (hasilPanenRepository.existsByWorker_IdAndTanggalPanen(currentWorker.getId(), today)) {
            throw new IllegalStateException("Buruh hanya dapat melaporkan hasil sekali sehari");
        }

        HasilPanen hasilPanen = new HasilPanen();
        hasilPanen.setWorker(currentWorker);
        hasilPanen.setTanggalPanen(today);
        hasilPanen.setKilogram(request.getKilogram());
        hasilPanen.setBerita(request.getBerita());
        hasilPanen.setStatus(HasilPanen.Status.Pending);
        hasilPanen.setCreatedAt(OffsetDateTime.now());

        HasilPanen saved = hasilPanenRepository.save(hasilPanen);

        for (MultipartFile file : request.getPhotos()) {
            String fileUrl = supabaseService.uploadPhoto(file);

            FotoHasilPanen foto = new FotoHasilPanen();
            foto.setHasilPanen(saved);
            foto.setUrl(fileUrl);
            foto.setUploadedAt(OffsetDateTime.now());

            fotoHasilPanenRepository.save(foto);
        }
        return mapToResponse(saved);
    }

    @Transactional
    public HarvestResponse approveHarvest(Long id, User currentMandor) {
        HasilPanen panen = validateMandorAuthorization(id, currentMandor);

        panen.setStatus(HasilPanen.Status.Approved);
        return mapToResponse(hasilPanenRepository.save(panen));
    }

    @Transactional
    public HarvestResponse rejectHarvest(Long id, String alasan, User currentMandor) {
        HasilPanen panen = validateMandorAuthorization(id, currentMandor);

        panen.setStatus(HasilPanen.Status.Rejected);
        panen.setRejectionReason(alasan);
        return mapToResponse(hasilPanenRepository.save(panen));
    }

    public List<HarvestResponse> getMyHarvestHistory(User currentWorker, LocalDate startDate, LocalDate endDate, String statusStr) {
        HasilPanen.Status statusEnum = null;
        if (statusStr != null && !statusStr.isBlank()) {
            statusEnum = HasilPanen.Status.valueOf(statusStr);
        }

        return hasilPanenRepository.findByWorkerIdWithFilters(currentWorker.getId(), startDate, endDate, statusEnum)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<HarvestResponse> getMandorHarvestHistory(User currentMandor, LocalDate startDate, LocalDate endDate, String statusStr, String workerName) {
        if (currentMandor.getRole() != User.Role.Mandor) {
            throw new IllegalStateException("Hanya Mandor yang dapat melihat daftar panen buruhnya");
        }

        HasilPanen.Status statusEnum = null;
        if (statusStr != null && !statusStr.isBlank()) {
            statusEnum = HasilPanen.Status.valueOf(statusStr);
        }

        return hasilPanenRepository.findForMandorWithFilters(currentMandor.getId(), startDate, endDate, statusEnum, workerName)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private HarvestResponse mapToResponse(HasilPanen panen) {
        List<String> urls = fotoHasilPanenRepository.findAllByHasilPanen_Id(panen.getId())
                .stream()
                .map(FotoHasilPanen::getUrl)
                .collect(Collectors.toList());

        return HarvestResponse.builder()
                .id(panen.getId())
                .tanggalPanen(panen.getTanggalPanen())
                .kilogram(panen.getKilogram())
                .berita(panen.getBerita())
                .status(panen.getStatus().name())
                .rejectionReason(panen.getRejectionReason())
                .fotoUrls(urls)
                .build();
    }
}