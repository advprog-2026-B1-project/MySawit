package com.b1.mysawit.kebun.service;

import com.b1.mysawit.common.exception.BusinessRuleViolationException;
import com.b1.mysawit.common.exception.DuplicateResourceException;
import com.b1.mysawit.common.exception.ResourceNotFoundException;
import com.b1.mysawit.domain.Kebun;
import com.b1.mysawit.kebun.dto.KebunCreateRequest;
import com.b1.mysawit.kebun.dto.KebunResponse;
import com.b1.mysawit.kebun.dto.KebunUpdateRequest;
import com.b1.mysawit.kebun.repository.KebunRepository;
import com.b1.mysawit.kebun.repository.MandorAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class KebunServiceImpl implements KebunService {

    private final KebunRepository kebunRepository;
    private final MandorAssignmentRepository mandorAssignmentRepository;
    private final KebunValidator kebunValidator;
    private final KebunMapper kebunMapper;

    public KebunServiceImpl(
            KebunRepository kebunRepository,
            MandorAssignmentRepository mandorAssignmentRepository,
            KebunValidator kebunValidator,
            KebunMapper kebunMapper) {
        this.kebunRepository = kebunRepository;
        this.mandorAssignmentRepository = mandorAssignmentRepository;
        this.kebunValidator = kebunValidator;
        this.kebunMapper = kebunMapper;
    }

    @Override
    public KebunResponse createKebun(KebunCreateRequest request) {
        kebunValidator.validateCreateRequest(request);
        checkKodeKebunNotDuplicate(request.getKodeKebun());

        Kebun kebun = kebunMapper.toEntity(request);
        Kebun saved = kebunRepository.save(kebun);
        return kebunMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KebunResponse> getAllKebun(String nama, String kode) {
        return findKebunByFilters(nama, kode).stream()
                .map(kebunMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public KebunResponse getKebunById(Long id) {
        Kebun kebun = findKebunOrThrow(id);
        return kebunMapper.toResponse(kebun);
    }

    @Override
    public KebunResponse updateKebun(Long id, KebunUpdateRequest request) {
        kebunValidator.validateUpdateRequest(request);
        Kebun kebun = findKebunOrThrow(id);
        applyUpdates(kebun, request);
        kebun.setUpdatedAt(OffsetDateTime.now());
        Kebun updated = kebunRepository.save(kebun);
        return kebunMapper.toResponse(updated);
    }

    @Override
    public void deleteKebun(Long id) {
        Kebun kebun = findKebunOrThrow(id);
        if (mandorAssignmentRepository.existsByKebunIdAndUnassignedAtIsNull(id)) {
            throw new BusinessRuleViolationException(
                    "Kebun tidak dapat dihapus karena masih terikat dengan seorang Mandor aktif");
        }
        kebunRepository.delete(kebun);
    }

    // ─── Private Helpers ────────────────────────────────────────────────────────

    private Kebun findKebunOrThrow(Long id) {
        return kebunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kebun", "id", id));
    }

    private List<Kebun> findKebunByFilters(String nama, String kode) {
        boolean hasNama = kebunValidator.hasValue(nama);
        boolean hasKode = kebunValidator.hasValue(kode);

        if (hasNama && hasKode) {
            return kebunRepository
                    .findByNamaKebunContainingIgnoreCaseAndKodeKebunContainingIgnoreCase(nama, kode);
        } else if (hasNama) {
            return kebunRepository.findByNamaKebunContainingIgnoreCase(nama);
        } else if (hasKode) {
            return kebunRepository.findByKodeKebunContainingIgnoreCase(kode);
        } else {
            return kebunRepository.findAll();
        }
    }

    private void applyUpdates(Kebun kebun, KebunUpdateRequest request) {
        // kodeKebun tidak bisa diubah — sesuai spesifikasi
        if (kebunValidator.hasValue(request.getNamaKebun())) {
            kebun.setNamaKebun(request.getNamaKebun().trim());
        }
        if (request.getLuasHektare() != null) {
            kebun.setLuasHektare(request.getLuasHektare());
        }
        if (request.getKoordinat() != null) {
            kebun.setKoordinat(request.getKoordinat());
        }
    }

    private void checkKodeKebunNotDuplicate(String kodeKebun) {
        if (kebunRepository.existsByKodeKebun(kodeKebun)) {
            throw new DuplicateResourceException("Kebun", "kodeKebun", kodeKebun);
        }
    }
}

