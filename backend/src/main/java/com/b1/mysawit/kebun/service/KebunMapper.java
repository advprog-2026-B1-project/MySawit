package com.b1.mysawit.kebun.service;

import com.b1.mysawit.domain.Kebun;
import com.b1.mysawit.kebun.dto.KebunCreateRequest;
import com.b1.mysawit.kebun.dto.KebunResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Komponen mapper untuk konversi antara entitas Kebun dan DTO.
 * Dipisahkan dari KebunServiceImpl agar setiap kelas hanya memiliki
 * satu tanggung jawab (Single Responsibility Principle).
 */
@Component
public class KebunMapper {

    private static final Pattern POINT = Pattern.compile("\\((-?[\\d.]+),\\s*(-?[\\d.]+)\\)");

    public Kebun toEntity(KebunCreateRequest request) {
        return Kebun.builder()
                .kodeKebun(request.getKodeKebun().trim())
                .namaKebun(request.getNamaKebun().trim())
                .luasHektare(calculateLuas(request.getKoordinat()))
                .koordinat(request.getKoordinat())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public KebunResponse toResponse(Kebun kebun) {
        return KebunResponse.fromEntity(kebun);
    }

    /**
     * Menghitung luas kebun dari 4 koordinat titik sudut.
     * Karena kebun axis-aligned, cukup (maxX - minX) × (maxY - minY).
     * Validasi bentuk persegi dilakukan sebelum kalkulasi.
     * Hasil dibagi 10.000 untuk konversi dari m² ke hektare.
     */
    public BigDecimal calculateLuas(String koordinat) {
        Matcher m = POINT.matcher(koordinat);
        List<double[]> pts = new ArrayList<>();
        while (m.find()) {
            pts.add(new double[]{Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2))});
        }
        validateSquare(pts);
        double width  = pts.stream().mapToDouble(p -> p[0]).max().orElse(0)
                      - pts.stream().mapToDouble(p -> p[0]).min().orElse(0);
        double height = pts.stream().mapToDouble(p -> p[1]).max().orElse(0)
                      - pts.stream().mapToDouble(p -> p[1]).min().orElse(0);
        return BigDecimal.valueOf(width * height / 10000).setScale(2, RoundingMode.HALF_UP);
    }

    private void validateSquare(List<double[]> pts) {
        if (pts.size() != 4) {
            throw new IllegalArgumentException("Koordinat kebun harus memiliki tepat 4 titik sudut");
        }
        // Kebun diasumsikan axis-aligned: harus ada tepat 2 nilai x unik dan 2 nilai y unik
        long uniqueX = pts.stream().mapToLong(p -> (long) p[0]).distinct().count();
        long uniqueY = pts.stream().mapToLong(p -> (long) p[1]).distinct().count();
        if (uniqueX != 2 || uniqueY != 2) {
            throw new IllegalArgumentException(
                    "Koordinat tidak valid: kebun harus sejajar sumbu koordinat " +
                    "(contoh: [(0,0),(100,0),(100,100),(0,100)])");
        }
        double minX = pts.stream().mapToDouble(p -> p[0]).min().orElse(0);
        double maxX = pts.stream().mapToDouble(p -> p[0]).max().orElse(0);
        double minY = pts.stream().mapToDouble(p -> p[1]).min().orElse(0);
        double maxY = pts.stream().mapToDouble(p -> p[1]).max().orElse(0);
        double width = maxX - minX;
        double height = maxY - minY;
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Koordinat kebun tidak valid: luas tidak boleh nol");
        }
        if (Math.abs(width - height) > 0.01) {
            throw new IllegalArgumentException(
                    "Kebun harus berbentuk persegi: lebar (" + (long) width +
                    ") harus sama dengan tinggi (" + (long) height + ")");
        }
    }
}
