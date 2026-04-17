package com.b1.mysawit.kebun.service;

import com.b1.mysawit.common.exception.BusinessRuleViolationException;
import com.b1.mysawit.common.exception.DuplicateResourceException;
import com.b1.mysawit.common.exception.ResourceNotFoundException;
import com.b1.mysawit.domain.Kebun;
import com.b1.mysawit.kebun.dto.KebunCreateRequest;
import com.b1.mysawit.kebun.dto.KebunKoordinatProjection;
import com.b1.mysawit.kebun.dto.KebunResponse;
import com.b1.mysawit.kebun.dto.KebunUpdateRequest;
import com.b1.mysawit.kebun.repository.KebunRepository;
import com.b1.mysawit.kebun.repository.MandorAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KebunServiceImpl Unit Tests")
class KebunServiceImplTest {

    @Mock
    private KebunRepository kebunRepository;

    @Mock
    private MandorAssignmentRepository mandorAssignmentRepository;

    // Plain components — di-instantiate langsung, tidak perlu di-mock
    private KebunValidator kebunValidator;
    private KebunMapper kebunMapper;
    private KebunOverlapValidator kebunOverlapValidator;

    private KebunServiceImpl kebunService;

    private Kebun kebunSample;

    // Koordinat non-overlap yang digunakan di happy-path tests
    private static final String KOORDINAT_A = "[(0,0),(100,0),(100,100),(0,100)]";
    // Koordinat kebun lain yang jauh (tidak overlap dengan A)
    private static final String KOORDINAT_FAR = "[(500,0),(600,0),(600,100),(500,100)]";
    // Koordinat yang overlap dengan A
    private static final String KOORDINAT_OVERLAP_WITH_A = "[(50,0),(150,0),(150,100),(50,100)]";

    // ─── Helper: membuat projection stub tanpa Mockito ────────────────────────
    private static KebunKoordinatProjection proj(Long id, String koordinat) {
        return new KebunKoordinatProjection() {
            @Override public Long getId() { return id; }
            @Override public String getKoordinat() { return koordinat; }
        };
    }

    @BeforeEach
    void setUp() {
        kebunValidator = new KebunValidator();
        kebunMapper = new KebunMapper();
        kebunOverlapValidator = new KebunOverlapValidator();
        kebunService = new KebunServiceImpl(
                kebunRepository,
                mandorAssignmentRepository,
                kebunValidator,
                kebunMapper,
                kebunOverlapValidator
        );
        kebunSample = Kebun.builder()
                .id(1L)
                .kodeKebun("KB001")
                .namaKebun("Kebun Sawit A")
                .luasHektare(new BigDecimal("10.5"))
                .koordinat(KOORDINAT_A)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    // ─── CREATE ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createKebun()")
    class CreateKebun {

        @Test
        @DisplayName("Given valid request, no existing kebun → should save and return KebunResponse")
        void givenValidRequest_noExisting_shouldSaveAndReturnResponse() {
            when(kebunRepository.existsByKodeKebun("KB001")).thenReturn(false);
            when(kebunRepository.findAllKoordinat()).thenReturn(List.of());
            when(kebunRepository.save(any(Kebun.class))).thenReturn(kebunSample);

            KebunResponse result = kebunService.createKebun(buildCreateRequest(KOORDINAT_A));

            assertThat(result).isNotNull();
            assertThat(result.getKodeKebun()).isEqualTo("KB001");
            assertThat(result.getNamaKebun()).isEqualTo("Kebun Sawit A");
            verify(kebunRepository, times(1)).save(any(Kebun.class));
        }

        @Test
        @DisplayName("Given valid request, existing kebun not overlapping → should save successfully")
        void givenValidRequest_existingNotOverlap_shouldSave() {
            when(kebunRepository.existsByKodeKebun("KB001")).thenReturn(false);
            when(kebunRepository.findAllKoordinat())
                    .thenReturn(List.of(proj(2L, KOORDINAT_FAR)));
            when(kebunRepository.save(any(Kebun.class))).thenReturn(kebunSample);

            assertThatCode(() -> kebunService.createKebun(buildCreateRequest(KOORDINAT_A)))
                    .doesNotThrowAnyException();

            verify(kebunRepository).save(any(Kebun.class));
        }

        @Test
        @DisplayName("Given koordinat overlap dengan kebun yang ada → throws BusinessRuleViolationException")
        void givenOverlapKoordinat_shouldThrowBusinessRuleViolationException() {
            when(kebunRepository.existsByKodeKebun("KB001")).thenReturn(false);
            when(kebunRepository.findAllKoordinat())
                    .thenReturn(List.of(proj(5L, KOORDINAT_OVERLAP_WITH_A)));

            assertThatThrownBy(() -> kebunService.createKebun(buildCreateRequest(KOORDINAT_A)))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("tumpang tindih");

            verify(kebunRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given koordinat bersinggungan di sisi (adjacent) → should save successfully")
        void givenAdjacentKoordinat_shouldSaveSuccessfully() {
            String adjacentKoordinat = "[(100,0),(200,0),(200,100),(100,100)]"; // tepat di sisi kanan A
            when(kebunRepository.existsByKodeKebun("KB001")).thenReturn(false);
            when(kebunRepository.findAllKoordinat())
                    .thenReturn(List.of(proj(2L, KOORDINAT_A)));
            Kebun adjacentKebun = Kebun.builder().id(1L).kodeKebun("KB001").namaKebun("Kebun B")
                    .luasHektare(new BigDecimal("10.0")).koordinat(adjacentKoordinat)
                    .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
            when(kebunRepository.save(any(Kebun.class))).thenReturn(adjacentKebun);

            assertThatCode(() -> kebunService.createKebun(KebunCreateRequest.builder()
                    .kodeKebun("KB001").namaKebun("Kebun B")
                    .luasHektare(new BigDecimal("10.0")).koordinat(adjacentKoordinat)
                    .build())).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Given duplicate kodeKebun → should throw DuplicateResourceException")
        void givenDuplicateKode_shouldThrowDuplicateResourceException() {
            when(kebunRepository.existsByKodeKebun("KB001")).thenReturn(true);

            assertThatThrownBy(() -> kebunService.createKebun(buildCreateRequest(KOORDINAT_A)))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("KB001");

            verify(kebunRepository, never()).save(any());
            // findAllKoordinat tidak boleh dipanggil jika kode sudah duplikat
            verify(kebunRepository, never()).findAllKoordinat();
        }

        @Test
        @DisplayName("Given null namaKebun → should throw IllegalArgumentException")
        void givenNullNama_shouldThrowIllegalArgumentException() {
            KebunCreateRequest request = KebunCreateRequest.builder()
                    .kodeKebun("KB001").namaKebun(null)
                    .luasHektare(new BigDecimal("10.5")).koordinat(KOORDINAT_A)
                    .build();

            assertThatThrownBy(() -> kebunService.createKebun(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nama kebun");
        }

        @Test
        @DisplayName("Given blank namaKebun → should throw IllegalArgumentException")
        void givenBlankNama_shouldThrowIllegalArgumentException() {
            KebunCreateRequest request = KebunCreateRequest.builder()
                    .kodeKebun("KB001").namaKebun("   ")
                    .luasHektare(new BigDecimal("10.5")).koordinat(KOORDINAT_A)
                    .build();

            assertThatThrownBy(() -> kebunService.createKebun(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nama kebun");
        }

        @Test
        @DisplayName("Given null kodeKebun → should throw IllegalArgumentException")
        void givenNullKode_shouldThrowIllegalArgumentException() {
            KebunCreateRequest request = KebunCreateRequest.builder()
                    .kodeKebun(null).namaKebun("Kebun A")
                    .luasHektare(new BigDecimal("10.5")).koordinat(KOORDINAT_A)
                    .build();

            assertThatThrownBy(() -> kebunService.createKebun(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Kode kebun");
        }

        @Test
        @DisplayName("Given zero luasHektare → should throw IllegalArgumentException")
        void givenZeroLuas_shouldThrowIllegalArgumentException() {
            KebunCreateRequest request = KebunCreateRequest.builder()
                    .kodeKebun("KB001").namaKebun("Kebun A")
                    .luasHektare(BigDecimal.ZERO).koordinat(KOORDINAT_A)
                    .build();

            assertThatThrownBy(() -> kebunService.createKebun(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Luas hektare");
        }

        @Test
        @DisplayName("Given negative luasHektare → should throw IllegalArgumentException")
        void givenNegativeLuas_shouldThrowIllegalArgumentException() {
            KebunCreateRequest request = KebunCreateRequest.builder()
                    .kodeKebun("KB001").namaKebun("Kebun A")
                    .luasHektare(new BigDecimal("-5")).koordinat(KOORDINAT_A)
                    .build();

            assertThatThrownBy(() -> kebunService.createKebun(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Luas hektare");
        }

        @Test
        @DisplayName("Given null luasHektare → should throw IllegalArgumentException")
        void givenNullLuas_shouldThrowIllegalArgumentException() {
            KebunCreateRequest request = KebunCreateRequest.builder()
                    .kodeKebun("KB001").namaKebun("Kebun A")
                    .luasHektare(null).koordinat(KOORDINAT_A)
                    .build();

            assertThatThrownBy(() -> kebunService.createKebun(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Luas hektare");
        }

        @Test
        @DisplayName("Given null koordinat → should throw IllegalArgumentException")
        void givenNullKoordinat_shouldThrowIllegalArgumentException() {
            KebunCreateRequest request = KebunCreateRequest.builder()
                    .kodeKebun("KB001").namaKebun("Kebun A")
                    .luasHektare(new BigDecimal("10.5")).koordinat(null)
                    .build();

            assertThatThrownBy(() -> kebunService.createKebun(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Koordinat");
        }

        @Test
        @DisplayName("Given blank koordinat → should throw IllegalArgumentException")
        void givenBlankKoordinat_shouldThrowIllegalArgumentException() {
            KebunCreateRequest request = KebunCreateRequest.builder()
                    .kodeKebun("KB001").namaKebun("Kebun A")
                    .luasHektare(new BigDecimal("10.5")).koordinat("   ")
                    .build();

            assertThatThrownBy(() -> kebunService.createKebun(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Koordinat");
        }

        private KebunCreateRequest buildCreateRequest(String koordinat) {
            return KebunCreateRequest.builder()
                    .kodeKebun("KB001").namaKebun("Kebun Sawit A")
                    .luasHektare(new BigDecimal("10.5")).koordinat(koordinat)
                    .build();
        }
    }

    // ─── GET ALL ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllKebun()")
    class GetAllKebun {

        @Test
        @DisplayName("Given 2 kebun exist → should return list of 2")
        void givenTwoKebun_shouldReturnListOfTwo() {
            Kebun kebun2 = Kebun.builder()
                    .id(2L).kodeKebun("KB002").namaKebun("Kebun Sawit B")
                    .luasHektare(new BigDecimal("20.0"))
                    .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                    .build();

            when(kebunRepository.findAll()).thenReturn(List.of(kebunSample, kebun2));

            List<KebunResponse> results = kebunService.getAllKebun(null, null);

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getKodeKebun()).isEqualTo("KB001");
            assertThat(results.get(1).getKodeKebun()).isEqualTo("KB002");
        }

        @Test
        @DisplayName("Given no kebun → should return empty list")
        void givenNoKebun_shouldReturnEmptyList() {
            when(kebunRepository.findAll()).thenReturn(List.of());

            List<KebunResponse> results = kebunService.getAllKebun(null, null);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Given filter by nama → should call findByNamaKebun")
        void givenFilterByNama_shouldCallCorrectRepository() {
            when(kebunRepository.findByNamaKebunContainingIgnoreCase("Sawit A"))
                    .thenReturn(List.of(kebunSample));

            List<KebunResponse> results = kebunService.getAllKebun("Sawit A", null);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getNamaKebun()).isEqualTo("Kebun Sawit A");
            verify(kebunRepository).findByNamaKebunContainingIgnoreCase("Sawit A");
        }

        @Test
        @DisplayName("Given filter by kode → should call findByKodeKebun")
        void givenFilterByKode_shouldCallCorrectRepository() {
            when(kebunRepository.findByKodeKebunContainingIgnoreCase("KB001"))
                    .thenReturn(List.of(kebunSample));

            List<KebunResponse> results = kebunService.getAllKebun(null, "KB001");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getKodeKebun()).isEqualTo("KB001");
            verify(kebunRepository).findByKodeKebunContainingIgnoreCase("KB001");
        }

        @Test
        @DisplayName("Given filter by nama and kode → should call combined repository method")
        void givenFilterByNamaAndKode_shouldCallCombinedMethod() {
            when(kebunRepository.findByNamaKebunContainingIgnoreCaseAndKodeKebunContainingIgnoreCase(
                    "Sawit", "KB001")).thenReturn(List.of(kebunSample));

            List<KebunResponse> results = kebunService.getAllKebun("Sawit", "KB001");

            assertThat(results).hasSize(1);
        }
    }

    // ─── GET BY ID ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getKebunById()")
    class GetKebunById {

        @Test
        @DisplayName("Given existing id → should return KebunResponse")
        void givenExistingId_shouldReturnKebunResponse() {
            when(kebunRepository.findById(1L)).thenReturn(Optional.of(kebunSample));

            KebunResponse result = kebunService.getKebunById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getKodeKebun()).isEqualTo("KB001");
        }

        @Test
        @DisplayName("Given non-existing id → should throw ResourceNotFoundException")
        void givenNonExistingId_shouldThrowResourceNotFoundException() {
            when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> kebunService.getKebunById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateKebun()")
    class UpdateKebun {

        @Test
        @DisplayName("Given valid update (nama + luas, no koordinat) → should return updated KebunResponse")
        void givenValidUpdate_noKoordinat_shouldReturnUpdatedResponse() {
            KebunUpdateRequest request = KebunUpdateRequest.builder()
                    .namaKebun("Kebun Updated")
                    .luasHektare(new BigDecimal("15.0"))
                    .build();

            when(kebunRepository.findById(1L)).thenReturn(Optional.of(kebunSample));
            when(kebunRepository.save(any(Kebun.class))).thenAnswer(inv -> inv.getArgument(0));

            KebunResponse result = kebunService.updateKebun(1L, request);

            assertThat(result.getNamaKebun()).isEqualTo("Kebun Updated");
            assertThat(result.getLuasHektare()).isEqualByComparingTo("15.0");
            // Tidak ada perubahan koordinat → findAllKoordinatExcluding tidak boleh dipanggil
            verify(kebunRepository, never()).findAllKoordinatExcluding(any());
        }

        @Test
        @DisplayName("Given update koordinat, no overlap → should save successfully")
        void givenUpdateKoordinat_noOverlap_shouldSave() {
            String newKoordinat = "[(200,0),(300,0),(300,100),(200,100)]";
            KebunUpdateRequest request = KebunUpdateRequest.builder()
                    .koordinat(newKoordinat)
                    .build();

            when(kebunRepository.findById(1L)).thenReturn(Optional.of(kebunSample));
            when(kebunRepository.findAllKoordinatExcluding(1L))
                    .thenReturn(List.of(proj(2L, KOORDINAT_FAR)));
            when(kebunRepository.save(any(Kebun.class))).thenAnswer(inv -> inv.getArgument(0));

            KebunResponse result = kebunService.updateKebun(1L, request);

            assertThat(result.getKoordinat()).isEqualTo(newKoordinat);
            verify(kebunRepository).findAllKoordinatExcluding(1L);
        }

        @Test
        @DisplayName("Given update koordinat yang overlap → throws BusinessRuleViolationException")
        void givenUpdateKoordinat_overlap_shouldThrow() {
            KebunUpdateRequest request = KebunUpdateRequest.builder()
                    .koordinat(KOORDINAT_A)  // sama persis dengan koordinat kebun id=2
                    .build();

            when(kebunRepository.findById(1L)).thenReturn(Optional.of(kebunSample));
            when(kebunRepository.findAllKoordinatExcluding(1L))
                    .thenReturn(List.of(proj(2L, KOORDINAT_OVERLAP_WITH_A)));

            assertThatThrownBy(() -> kebunService.updateKebun(1L, request))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("tumpang tindih");

            verify(kebunRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given update → kodeKebun TIDAK berubah meski apapun dikirim")
        void givenUpdate_kodeKebunShouldNotChange() {
            KebunUpdateRequest request = KebunUpdateRequest.builder()
                    .namaKebun("Updated Name")
                    .build();

            when(kebunRepository.findById(1L)).thenReturn(Optional.of(kebunSample));
            when(kebunRepository.save(any(Kebun.class))).thenAnswer(inv -> inv.getArgument(0));

            KebunResponse result = kebunService.updateKebun(1L, request);

            // kodeKebun harus tetap KB001, tidak berubah
            assertThat(result.getKodeKebun()).isEqualTo("KB001");
        }

        @Test
        @DisplayName("Given non-existing id → should throw ResourceNotFoundException")
        void givenNonExistingId_shouldThrowResourceNotFoundException() {
            when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> kebunService.updateKebun(99L,
                    KebunUpdateRequest.builder().namaKebun("Updated").build()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Given negative luasHektare in update → should throw IllegalArgumentException")
        void givenNegativeLuasUpdate_shouldThrowIllegalArgumentException() {
            // validator gagal sebelum findById → tidak perlu stub apapun
            assertThatThrownBy(() -> kebunService.updateKebun(1L,
                    KebunUpdateRequest.builder().luasHektare(new BigDecimal("-1")).build()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Luas hektare");
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteKebun()")
    class DeleteKebun {

        @Test
        @DisplayName("Given existing id with no active mandor → should delete successfully")
        void givenExistingId_noActiveMandor_shouldDeleteSuccessfully() {
            when(kebunRepository.findById(1L)).thenReturn(Optional.of(kebunSample));
            when(mandorAssignmentRepository.existsByKebunIdAndUnassignedAtIsNull(1L)).thenReturn(false);

            assertThatCode(() -> kebunService.deleteKebun(1L))
                    .doesNotThrowAnyException();

            verify(kebunRepository, times(1)).delete(kebunSample);
        }

        @Test
        @DisplayName("Given kebun still has active mandor → should throw BusinessRuleViolationException")
        void givenKebunWithActiveMandor_shouldThrowBusinessRuleViolationException() {
            when(kebunRepository.findById(1L)).thenReturn(Optional.of(kebunSample));
            when(mandorAssignmentRepository.existsByKebunIdAndUnassignedAtIsNull(1L)).thenReturn(true);

            assertThatThrownBy(() -> kebunService.deleteKebun(1L))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Mandor aktif");

            verify(kebunRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Given non-existing id → should throw ResourceNotFoundException")
        void givenNonExistingId_shouldThrowResourceNotFoundException() {
            when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> kebunService.deleteKebun(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(kebunRepository, never()).delete(any());
        }
    }
}
