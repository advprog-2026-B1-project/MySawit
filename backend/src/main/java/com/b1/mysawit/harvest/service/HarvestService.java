package com.b1.mysawit.harvest.service;

import com.b1.mysawit.domain.HasilPanen;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.harvest.dto.HarvestRequest;
import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.repository.HasilPanenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HarvestService {

    private final HasilPanenRepository hasilPanenRepository;

    @Transactional
    public HarvestResponse createHarvest(User currentWorker, HarvestRequest request) {
        LocalDate today = LocalDate.now();

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
        return mapToResponse(saved);
    }

    public List<HarvestResponse> getMyHarvestHistory(User currentWorker) {
        return hasilPanenRepository.findAllByWorker_IdOrderByTanggalPanenDesc(currentWorker.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private HarvestResponse mapToResponse(HasilPanen panen) {
        return HarvestResponse.builder()
                .id(panen.getId())
                .tanggalPanen(panen.getTanggalPanen())
                .kilogram(panen.getKilogram())
                .berita(panen.getBerita())
                .status(panen.getStatus().name())
                .build();
    }
}