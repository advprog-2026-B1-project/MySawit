package com.b1.mysawit.kebun.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KebunMapperTest {

    private final KebunMapper mapper = new KebunMapper();

    @Test
    void calculateLuas_validSquare_returnsCorrectHectare() {
        // 100x100 = 10000 m² = 1 ha
        BigDecimal luas = mapper.calculateLuas("[(0,0),(100,0),(100,100),(0,100)]");
        assertThat(luas).isEqualByComparingTo("1.00");
    }

    @Test
    void calculateLuas_validSquareAnyPointOrder_returnsCorrectHectare() {
        // Titik tidak berurutan tapi tetap valid
        BigDecimal luas = mapper.calculateLuas("[(0,0),(0,100),(100,0),(100,100)]");
        assertThat(luas).isEqualByComparingTo("1.00");
    }

    @Test
    void calculateLuas_negativeCoordinates_returnsCorrectHectare() {
        // 100x100 dengan koordinat negatif = 1 ha
        BigDecimal luas = mapper.calculateLuas("[(-100,-100),(-100,0),(0,-100),(0,0)]");
        assertThat(luas).isEqualByComparingTo("1.00");
    }

    @Test
    void calculateLuas_largeSquare_returnsCorrectHectare() {
        // 200x200 = 40000 m² = 4 ha
        BigDecimal luas = mapper.calculateLuas("[(0,0),(200,0),(200,200),(0,200)]");
        assertThat(luas).isEqualByComparingTo("4.00");
    }

    @Test
    void calculateLuas_lessThan4Points_throwsIllegalArgument() {
        assertThatThrownBy(() -> mapper.calculateLuas("[(0,0),(100,0),(100,100)]"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("4 titik sudut");
    }

    @Test
    void calculateLuas_moreThan4Points_throwsIllegalArgument() {
        assertThatThrownBy(() -> mapper.calculateLuas("[(0,0),(100,0),(100,100),(0,100),(50,50)]"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("4 titik sudut");
    }

    @Test
    void calculateLuas_notAxisAligned_uniqueXNotTwo_throwsIllegalArgument() {
        // Semua titik punya x berbeda — tidak axis-aligned
        assertThatThrownBy(() -> mapper.calculateLuas("[(0,0),(50,0),(100,100),(150,100)]"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sejajar sumbu koordinat");
    }

    @Test
    void calculateLuas_notAxisAligned_uniqueYNotTwo_throwsIllegalArgument() {
        // Semua titik punya y berbeda
        assertThatThrownBy(() -> mapper.calculateLuas("[(0,0),(100,0),(100,50),(0,100)]"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sejajar sumbu koordinat");
    }

    @Test
    void calculateLuas_rectangle_notSquare_throwsIllegalArgument() {
        // 100x200 = persegi panjang, bukan persegi
        assertThatThrownBy(() -> mapper.calculateLuas("[(0,0),(100,0),(100,200),(0,200)]"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("persegi");
    }

    @Test
    void calculateLuas_zeroWidth_throwsIllegalArgument() {
        // Semua x sama → width = 0
        assertThatThrownBy(() -> mapper.calculateLuas("[(0,0),(0,0),(0,100),(0,100)]"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
