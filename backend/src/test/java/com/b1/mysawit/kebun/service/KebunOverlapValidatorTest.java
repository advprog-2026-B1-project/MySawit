package com.b1.mysawit.kebun.service;

import com.b1.mysawit.common.exception.BusinessRuleViolationException;
import com.b1.mysawit.kebun.dto.KebunKoordinatProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("KebunOverlapValidator Unit Tests")
class KebunOverlapValidatorTest {

    private KebunOverlapValidator validator;

    // ─── Helper: membuat projection stub tanpa Mockito ───────────────────────
    private static KebunKoordinatProjection proj(Long id, String koordinat) {
        return new KebunKoordinatProjection() {
            @Override public Long getId() { return id; }
            @Override public String getKoordinat() { return koordinat; }
        };
    }

    @BeforeEach
    void setUp() {
        validator = new KebunOverlapValidator();
    }

    // ─── parseKoordinat ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("parseKoordinat()")
    class ParseKoordinat {

        @Test
        @DisplayName("Format tanpa spasi → parse berhasil")
        void noSpaces_shouldParseCorrectly() {
            double[][] result = validator.parseKoordinat("[(0,0),(100,0),(100,100),(0,100)]");

            assertThat(result).hasNumberOfRows(4);
            assertThat(result[0]).containsExactly(0.0, 0.0);
            assertThat(result[1]).containsExactly(100.0, 0.0);
            assertThat(result[2]).containsExactly(100.0, 100.0);
            assertThat(result[3]).containsExactly(0.0, 100.0);
        }

        @Test
        @DisplayName("Format dengan spasi (sesuai spesifikasi) → parse berhasil")
        void withSpaces_shouldParseCorrectly() {
            double[][] result = validator.parseKoordinat("[(0, 0), (200, 0), (200, 200), (0, 200)]");

            assertThat(result).hasNumberOfRows(4);
            assertThat(result[0]).containsExactly(0.0, 0.0);
            assertThat(result[2]).containsExactly(200.0, 200.0);
        }

        @Test
        @DisplayName("Koordinat negatif (Lat/Lon nyata) → parse berhasil")
        void negativeCoordinates_shouldParseCorrectly() {
            double[][] result = validator.parseKoordinat("[(-6.2,-106.8),(-6.2,-106.5),(-5.9,-106.5),(-5.9,-106.8)]");

            assertThat(result[0][0]).isEqualTo(-6.2);
            assertThat(result[0][1]).isEqualTo(-106.8);
        }

        @Test
        @DisplayName("Kurang dari 4 titik → throws IllegalArgumentException")
        void lessThanFourPoints_shouldThrow() {
            assertThatThrownBy(() -> validator.parseKoordinat("[(0,0),(100,0),(100,100)]"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("4 titik");
        }

        @Test
        @DisplayName("Lebih dari 4 titik → throws IllegalArgumentException")
        void moreThanFourPoints_shouldThrow() {
            assertThatThrownBy(() ->
                    validator.parseKoordinat("[(0,0),(100,0),(100,100),(0,100),(50,50)]"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("4 titik");
        }

        @Test
        @DisplayName("Nilai non-numerik → throws IllegalArgumentException")
        void nonNumericValue_shouldThrow() {
            assertThatThrownBy(() ->
                    validator.parseKoordinat("[(0,0),(abc,0),(100,100),(0,100)]"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tidak valid");
        }

        @Test
        @DisplayName("Koordinat null → throws IllegalArgumentException")
        void nullInput_shouldThrow() {
            assertThatThrownBy(() -> validator.parseKoordinat(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("kosong");
        }

        @Test
        @DisplayName("Koordinat blank → throws IllegalArgumentException")
        void blankInput_shouldThrow() {
            assertThatThrownBy(() -> validator.parseKoordinat("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("kosong");
        }
    }

    // ─── polygonsOverlap ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("polygonsOverlap()")
    class PolygonsOverlap {

        // Kebun A: (0,0) → (100,100)
        private final double[][] polyA = {
                {0, 0}, {100, 0}, {100, 100}, {0, 100}
        };

        @Test
        @DisplayName("Dua persegi tidak beririsan → return false")
        void separateRectangles_shouldNotOverlap() {
            // Kebun B berada di sebelah kanan A, tidak menyentuh
            double[][] polyB = {{200, 0}, {300, 0}, {300, 100}, {200, 100}};

            assertThat(validator.polygonsOverlap(polyA, polyB)).isFalse();
        }

        @Test
        @DisplayName("Dua persegi identik (persis sama) → return true")
        void identicalRectangles_shouldOverlap() {
            double[][] polyB = {{0, 0}, {100, 0}, {100, 100}, {0, 100}};

            assertThat(validator.polygonsOverlap(polyA, polyB)).isTrue();
        }

        @Test
        @DisplayName("Persegi B di dalam persegi A (contained) → return true")
        void containedRectangle_shouldOverlap() {
            double[][] polyB = {{10, 10}, {90, 10}, {90, 90}, {10, 90}};

            assertThat(validator.polygonsOverlap(polyA, polyB)).isTrue();
        }

        @Test
        @DisplayName("Persegi B overlap sebagian dengan A → return true")
        void partialOverlap_shouldOverlap() {
            // B mulai dari x=50 — overlap dengan separuh kanan A
            double[][] polyB = {{50, 0}, {150, 0}, {150, 100}, {50, 100}};

            assertThat(validator.polygonsOverlap(polyA, polyB)).isTrue();
        }

        @Test
        @DisplayName("Bersinggungan tepat di sisi (adjacent edge) → return false")
        void touchingEdge_shouldNotOverlap() {
            // Kebun B mulai tepat di x=100 — edge A dan B bertemu persis
            double[][] polyB = {{100, 0}, {200, 0}, {200, 100}, {100, 100}};

            assertThat(validator.polygonsOverlap(polyA, polyB)).isFalse();
        }

        @Test
        @DisplayName("Bersinggungan tepat di sisi atas (adjacent top) → return false")
        void touchingTopEdge_shouldNotOverlap() {
            // Kebun B mulai tepat di y=100 — di atas A
            double[][] polyB = {{0, 100}, {100, 100}, {100, 200}, {0, 200}};

            assertThat(validator.polygonsOverlap(polyA, polyB)).isFalse();
        }

        @Test
        @DisplayName("Persegi dipisahkan hanya di sumbu Y → return false")
        void separatedOnYAxis_shouldNotOverlap() {
            double[][] polyB = {{0, 200}, {100, 200}, {100, 300}, {0, 300}};

            assertThat(validator.polygonsOverlap(polyA, polyB)).isFalse();
        }

        @Test
        @DisplayName("Overlap di pojok (corner overlap) → return true")
        void cornerOverlap_shouldOverlap() {
            // B overlap sedikit di pojok kanan-bawah A
            double[][] polyB = {{50, 50}, {150, 50}, {150, 150}, {50, 150}};

            assertThat(validator.polygonsOverlap(polyA, polyB)).isTrue();
        }
    }

    // ─── validateNoOverlap ───────────────────────────────────────────────────

    @Nested
    @DisplayName("validateNoOverlap()")
    class ValidateNoOverlap {

        private static final String KEBUN_A = "[(0,0),(100,0),(100,100),(0,100)]";
        private static final String KEBUN_B_ADJACENT = "[(100,0),(200,0),(200,100),(100,100)]";
        private static final String KEBUN_B_OVERLAP = "[(50,0),(150,0),(150,100),(50,100)]";
        private static final String KEBUN_C_FAR = "[(500,0),(600,0),(600,100),(500,100)]";

        @Test
        @DisplayName("List kosong → tidak throw exception")
        void emptyList_shouldNotThrow() {
            assertThatCode(() -> validator.validateNoOverlap(KEBUN_A, List.of()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("List null → tidak throw exception")
        void nullList_shouldNotThrow() {
            assertThatCode(() -> validator.validateNoOverlap(KEBUN_A, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Kebun baru tidak overlap dengan yang ada → tidak throw exception")
        void noOverlapWithExisting_shouldNotThrow() {
            List<KebunKoordinatProjection> existing = List.of(
                    proj(1L, KEBUN_C_FAR)
            );

            assertThatCode(() -> validator.validateNoOverlap(KEBUN_A, existing))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Kebun baru bersinggungan di sisi (adjacent) → tidak throw exception")
        void adjacentWithExisting_shouldNotThrow() {
            List<KebunKoordinatProjection> existing = List.of(
                    proj(1L, KEBUN_B_ADJACENT)
            );

            assertThatCode(() -> validator.validateNoOverlap(KEBUN_A, existing))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Kebun baru overlap dengan satu kebun → throws BusinessRuleViolationException")
        void overlapWithOne_shouldThrow() {
            List<KebunKoordinatProjection> existing = List.of(
                    proj(7L, KEBUN_B_OVERLAP)
            );

            assertThatThrownBy(() -> validator.validateNoOverlap(KEBUN_A, existing))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("tumpang tindih")
                    .hasMessageContaining("7");
        }

        @Test
        @DisplayName("Kebun baru overlap dengan salah satu dari banyak kebun → throws exception")
        void overlapWithOneAmongMany_shouldThrow() {
            List<KebunKoordinatProjection> existing = List.of(
                    proj(1L, KEBUN_C_FAR),         // jauh, tidak overlap
                    proj(2L, KEBUN_B_ADJACENT),     // menyentuh sisi, OK
                    proj(3L, KEBUN_B_OVERLAP)       // overlap!
            );

            assertThatThrownBy(() -> validator.validateNoOverlap(KEBUN_A, existing))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("3");
        }

        @Test
        @DisplayName("Update: koordinat sama dengan milik sendiri (excludeId sudah dibuang) → tidak throw")
        void updateSameCoordinates_afterSelfExcluded_shouldNotThrow() {
            // Simulasi: saat update, kebun dengan id ini sudah tidak ada di existing list
            // (karena query findAllKoordinatExcluding(id) sudah menyaringnya)
            List<KebunKoordinatProjection> existingWithoutSelf = List.of(
                    proj(2L, KEBUN_C_FAR)
            );

            assertThatCode(() -> validator.validateNoOverlap(KEBUN_A, existingWithoutSelf))
                    .doesNotThrowAnyException();
        }
    }
}
