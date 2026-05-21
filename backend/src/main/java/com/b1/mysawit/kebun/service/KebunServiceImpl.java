package com.b1.mysawit.kebun.service;

import com.b1.mysawit.auth.facade.UserFacade;
import com.b1.mysawit.auth.facade.UserSummary;
import com.b1.mysawit.common.exception.BusinessRuleViolationException;
import com.b1.mysawit.common.exception.DuplicateResourceException;
import com.b1.mysawit.common.exception.ResourceNotFoundException;
import com.b1.mysawit.domain.DriverAssignment;
import com.b1.mysawit.domain.Kebun;
import com.b1.mysawit.domain.MandorAssignment;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.kebun.dto.KebunCreateRequest;
import com.b1.mysawit.kebun.dto.KebunDashboardItem;
import com.b1.mysawit.kebun.dto.KebunDetailResponse;
import com.b1.mysawit.kebun.dto.KebunResponse;
import com.b1.mysawit.kebun.dto.KebunUpdateRequest;
import com.b1.mysawit.kebun.repository.DriverAssignmentRepository;
import com.b1.mysawit.kebun.repository.KebunRepository;
import com.b1.mysawit.kebun.repository.MandorAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class KebunServiceImpl implements KebunService {

    private static final Logger log = LoggerFactory.getLogger(KebunServiceImpl.class);

    private final KebunRepository kebunRepository;
    private final MandorAssignmentRepository mandorAssignmentRepository;
    private final DriverAssignmentRepository driverAssignmentRepository;
    private final KebunValidator kebunValidator;
    private final KebunMapper kebunMapper;
    private final KebunOverlapValidator kebunOverlapValidator;
    private final UserFacade userFacade;

    public KebunServiceImpl(
            KebunRepository kebunRepository,
            MandorAssignmentRepository mandorAssignmentRepository,
            DriverAssignmentRepository driverAssignmentRepository,
            KebunValidator kebunValidator,
            KebunMapper kebunMapper,
            KebunOverlapValidator kebunOverlapValidator,
            UserFacade userFacade) {
        this.kebunRepository = kebunRepository;
        this.mandorAssignmentRepository = mandorAssignmentRepository;
        this.driverAssignmentRepository = driverAssignmentRepository;
        this.kebunValidator = kebunValidator;
        this.kebunMapper = kebunMapper;
        this.kebunOverlapValidator = kebunOverlapValidator;
        this.userFacade = userFacade;
    }

    @Override
    public KebunResponse createKebun(KebunCreateRequest request) {
        kebunValidator.validateCreateRequest(request);
        checkKodeKebunNotDuplicate(request.getKodeKebun());
        kebunOverlapValidator.validateNoOverlap(
                request.getKoordinat(),
                kebunRepository.findAllKoordinat()
        );
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
    @Transactional(readOnly = true)
    public KebunDetailResponse getKebunDetail(Long id, String searchNamaSupir) {
        Kebun kebun = findKebunOrThrow(id);

        UserSummary mandor = mandorAssignmentRepository
                .findByKebunIdAndUnassignedAtIsNull(id)
                .flatMap(a -> userFacade.findUserSummaryById(a.getMandor().getId()))
                .orElse(null);

        List<Long> supirIds = driverAssignmentRepository
                .findAllByKebunIdAndUnassignedAtIsNull(id).stream()
                .map(a -> a.getDriver().getId())
                .collect(Collectors.toList());

        List<UserSummary> supirList = userFacade.findUserSummariesByIds(supirIds, searchNamaSupir);

        return KebunDetailResponse.builder()
                .id(kebun.getId())
                .kodeKebun(kebun.getKodeKebun())
                .namaKebun(kebun.getNamaKebun())
                .luasHektare(kebun.getLuasHektare())
                .koordinat(kebun.getKoordinat())
                .createdAt(kebun.getCreatedAt())
                .updatedAt(kebun.getUpdatedAt())
                .mandor(mandor)
                .supirList(supirList)
                .build();
    }

    @Override
    public KebunResponse updateKebun(Long id, KebunUpdateRequest request) {
        kebunValidator.validateUpdateRequest(request);
        Kebun kebun = findKebunOrThrow(id);
        // Cek overlap hanya jika koordinat memang diubah
        if (request.getKoordinat() != null) {
            kebunOverlapValidator.validateNoOverlap(
                    request.getKoordinat(),
                    kebunRepository.findAllKoordinatExcluding(id)
            );
        }
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

    @Override
    public void assignMandor(Long mandorId, Long kebunId) {
        validateAssignInput(mandorId, kebunId, "Mandor");
        userFacade.validateMandorExists(mandorId);
        Kebun kebun = findKebunOrThrow(kebunId);

        if (mandorAssignmentRepository.existsByMandorIdAndUnassignedAtIsNull(mandorId)) {
            throw new BusinessRuleViolationException(
                    "Mandor dengan id '" + mandorId + "' masih aktif di kebun lain");
        }

        MandorAssignment assignment = MandorAssignment.builder()
                .mandor(toUserReference(mandorId))
                .kebun(kebun)
                .assignedAt(OffsetDateTime.now())
                .unassignedAt(null)
                .build();
        mandorAssignmentRepository.save(assignment);
    }

    @Override
    public void assignSupir(Long supirId, Long kebunId) {
        validateAssignInput(supirId, kebunId, "Supir");
        userFacade.validateSupirExists(supirId);
        Kebun kebun = findKebunOrThrow(kebunId);

        if (driverAssignmentRepository.existsByDriverIdAndUnassignedAtIsNull(supirId)) {
            throw new BusinessRuleViolationException(
                    "Supir dengan id '" + supirId + "' masih aktif di kebun lain");
        }

        DriverAssignment assignment = DriverAssignment.builder()
                .driver(toUserReference(supirId))
                .kebun(kebun)
                .assignedAt(OffsetDateTime.now())
                .unassignedAt(null)
                .build();
        driverAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void reassignMandor(Long mandorId, Long oldKebunId, Long newKebunId) {
        validateReassignInput(mandorId, oldKebunId, newKebunId, "Mandor");
        userFacade.validateMandorExists(mandorId);
        findKebunOrThrow(oldKebunId);
        Kebun newKebun = findKebunOrThrow(newKebunId);

        MandorAssignment activeOldAssignment = mandorAssignmentRepository
                .findByMandorIdAndKebunIdAndUnassignedAtIsNull(mandorId, oldKebunId)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "Mandor dengan id '" + mandorId
                                + "' tidak sedang aktif di kebun lama"));

        OffsetDateTime now = OffsetDateTime.now();
        activeOldAssignment.setUnassignedAt(now);
        mandorAssignmentRepository.save(activeOldAssignment);

        if (mandorAssignmentRepository.existsByMandorIdAndUnassignedAtIsNull(mandorId)) {
            throw new BusinessRuleViolationException(
                    "Mandor dengan id '" + mandorId + "' masih aktif di kebun lain");
        }

        MandorAssignment newAssignment = MandorAssignment.builder()
                .mandor(toUserReference(mandorId))
                .kebun(newKebun)
                .assignedAt(now)
                .unassignedAt(null)
                .build();
        mandorAssignmentRepository.save(newAssignment);
    }

    @Override
    @Transactional
    public void reassignSupir(Long supirId, Long oldKebunId, Long newKebunId) {
        validateReassignInput(supirId, oldKebunId, newKebunId, "Supir");
        userFacade.validateSupirExists(supirId);
        findKebunOrThrow(oldKebunId);
        Kebun newKebun = findKebunOrThrow(newKebunId);

        DriverAssignment activeOldAssignment = driverAssignmentRepository
                .findByDriverIdAndKebunIdAndUnassignedAtIsNull(supirId, oldKebunId)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "Supir dengan id '" + supirId
                                + "' tidak sedang aktif di kebun lama"));

        OffsetDateTime now = OffsetDateTime.now();
        activeOldAssignment.setUnassignedAt(now);
        driverAssignmentRepository.save(activeOldAssignment);

        if (driverAssignmentRepository.existsByDriverIdAndUnassignedAtIsNull(supirId)) {
            throw new BusinessRuleViolationException(
                    "Supir dengan id '" + supirId + "' masih aktif di kebun lain");
        }

        DriverAssignment newAssignment = DriverAssignment.builder()
                .driver(toUserReference(supirId))
                .kebun(newKebun)
                .assignedAt(now)
                .unassignedAt(null)
                .build();
        driverAssignmentRepository.save(newAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KebunDashboardItem> getDashboard(boolean naive) {
        long start = System.currentTimeMillis();
        List<KebunDashboardItem> result;

        if (naive) {
            // N+1: 1 query untuk list kebun, lalu 2 query per kebun untuk count
            List<com.b1.mysawit.domain.Kebun> kebunList = kebunRepository.findAll();
            result = kebunList.stream().map(k -> KebunDashboardItem.builder()
                    .id(k.getId())
                    .kodeKebun(k.getKodeKebun())
                    .namaKebun(k.getNamaKebun())
                    .luasHektare(k.getLuasHektare())
                    .countMandorAktif(mandorAssignmentRepository.countByKebunIdAndUnassignedAtIsNull(k.getId()))
                    .countSupirAktif(driverAssignmentRepository.countByKebunIdAndUnassignedAtIsNull(k.getId()))
                    .build()
            ).collect(Collectors.toList());
        } else {
            // Optimized: 1 query dengan LEFT JOIN + COUNT GROUP BY
            result = kebunRepository.findDashboardOptimized();
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("[PROFILING] getDashboard strategy={} kebun={} elapsed={}ms",
                naive ? "naive" : "optimized", result.size(), elapsed);

        return result;
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
        // kodeKebun tidak dapat diubah — field ini tidak ada di KebunUpdateRequest
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

    private void validateAssignInput(Long userId, Long kebunId, String roleName) {
        if (userId == null) {
            throw new IllegalArgumentException(roleName + " id tidak boleh kosong");
        }
        if (kebunId == null) {
            throw new IllegalArgumentException("kebunId tidak boleh kosong");
        }
    }

    private void validateReassignInput(
            Long userId,
            Long oldKebunId,
            Long newKebunId,
            String roleName) {
        if (userId == null) {
            throw new IllegalArgumentException(roleName + " id tidak boleh kosong");
        }
        if (oldKebunId == null || newKebunId == null) {
            throw new IllegalArgumentException("oldKebunId dan newKebunId tidak boleh kosong");
        }
        if (oldKebunId.equals(newKebunId)) {
            throw new IllegalArgumentException("Kebun lama dan kebun baru harus berbeda");
        }
    }

    private User toUserReference(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }
}
