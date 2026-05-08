package com.b1.mysawit.harvest.service;

import com.b1.mysawit.domain.FotoHasilPanen;
import com.b1.mysawit.domain.HasilPanen;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.harvest.dto.HarvestRequest;
import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.repository.FotoHasilPanenRepository;
import com.b1.mysawit.repository.HasilPanenRepository;
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
    public HarvestResponse approveHarvest(Long id) {
        HasilPanen panen = hasilPanenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Data panen tidak ditemukan"));

        panen.setStatus(HasilPanen.Status.Approved);
        return mapToResponse(hasilPanenRepository.save(panen));
    }

    @Transactional
    public HarvestResponse rejectHarvest(Long id, String alasan) {
        HasilPanen panen = hasilPanenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Data panen tidak ditemukan"));

        panen.setStatus(HasilPanen.Status.Rejected);
        panen.setRejectionReason(alasan);
        return mapToResponse(hasilPanenRepository.save(panen));
    }

    public List<HarvestResponse> getMyHarvestHistory(User currentWorker) {
        return hasilPanenRepository.findAllByWorker_IdOrderByTanggalPanenDesc(currentWorker.getId())
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