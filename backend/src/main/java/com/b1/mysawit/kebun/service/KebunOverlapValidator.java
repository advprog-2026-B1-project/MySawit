package com.b1.mysawit.kebun.service;

import com.b1.mysawit.common.exception.BusinessRuleViolationException;
import com.b1.mysawit.kebun.dto.KebunKoordinatProjection;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Komponen validasi geometri untuk mendeteksi overlap antar-kebun.
 *
 * Algoritma: Separating Axis Theorem (SAT) untuk convex polygon.
 * SAT dipilih karena:
 *   - Berlaku untuk semua convex polygon (termasuk persegi yang dirotasi).
 *   - O(n) per pasangan polygon — sangat cepat untuk jumlah kebun yang realistis.
 *   - Kebun yang bersinggungan tepat di sisi (adjacent) dianggap TIDAK overlap (strict <).
 *
 * Format koordinat yang diterima: "[(lat,lon),(lat,lon),(lat,lon),(lat,lon)]"
 * Spasi di dalam string diabaikan oleh parser.
 */
@Component
public class KebunOverlapValidator {

    /**
     * Memvalidasi bahwa koordinat kebun baru tidak overlap dengan kebun-kebun yang ada.
     *
     * @param newKoordinat koordinat kebun yang akan disimpan/diupdate
     * @param existingRows hasil query projection berisi (id, koordinat) kebun lain
     * @throws BusinessRuleViolationException jika ditemukan overlap
     */
    public void validateNoOverlap(String newKoordinat, List<KebunKoordinatProjection> existingRows) {
        if (existingRows == null || existingRows.isEmpty()) {
            return;
        }
        double[][] newPoly = parseKoordinat(newKoordinat);
        for (KebunKoordinatProjection existing : existingRows) {
            double[][] existingPoly = parseKoordinat(existing.getKoordinat());
            if (polygonsOverlap(newPoly, existingPoly)) {
                throw new BusinessRuleViolationException(
                        "Koordinat kebun tumpang tindih dengan kebun yang sudah ada (id: "
                                + existing.getId() + ")");
            }
        }
    }

    /**
     * Mem-parse string koordinat menjadi array of [latitude, longitude].
     * Input: "[(lat,lon),(lat,lon),(lat,lon),(lat,lon)]"
     * Output: double[4][2] — 4 titik, masing-masing [lat, lon]
     */
    double[][] parseKoordinat(String koordinat) {
        if (koordinat == null || koordinat.isBlank()) {
            throw new IllegalArgumentException("Koordinat tidak boleh kosong");
        }

        String cleaned = koordinat
                .replaceAll("\\s", "")   // hapus semua whitespace
                .replaceAll("^\\[", "")  // hapus [ di awal
                .replaceAll("]$", "");   // hapus ] di akhir

        // cleaned: "(lat,lon),(lat,lon),(lat,lon),(lat,lon)"
        String[] parts = cleaned.split("\\),\\(");

        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "Koordinat harus memiliki tepat 4 titik sudut, ditemukan: " + parts.length);
        }

        double[][] points = new double[4][2];
        for (int i = 0; i < 4; i++) {
            String part = parts[i].replaceAll("[()]", "");
            String[] coords = part.split(",");
            if (coords.length != 2) {
                throw new IllegalArgumentException(
                        "Setiap titik koordinat harus memiliki 2 nilai (Latitude, Longitude)");
            }
            try {
                points[i][0] = Double.parseDouble(coords[0].trim()); // latitude
                points[i][1] = Double.parseDouble(coords[1].trim()); // longitude
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Nilai koordinat tidak valid pada titik ke-" + (i + 1) + ": " + part);
            }
        }
        return points;
    }

    /**
     * Menentukan apakah dua convex polygon benar-benar overlap (bukan hanya bersinggungan).
     * Menggunakan SAT: overlap terjadi jika DAN HANYA JIKA tidak ada separating axis dari
     * edges salah satu polygon.
     *
     * Kebun yang bersinggungan tepat di sisi dianggap valid (tidak overlap).
     */
    boolean polygonsOverlap(double[][] polyA, double[][] polyB) {
        return !hasSeparatingAxisFromEdges(polyA, polyB)
                && !hasSeparatingAxisFromEdges(polyB, polyA);
    }

    /**
     * Mencari separating axis dari edge-edge polygon sumber terhadap polygon target.
     * Jika ditemukan satu saja separating axis, kedua polygon TIDAK overlap.
     *
     * @return true jika ditemukan separating axis (polygon TIDAK overlap)
     */
    private boolean hasSeparatingAxisFromEdges(double[][] source, double[][] target) {
        int n = source.length;
        for (int i = 0; i < n; i++) {
            double ax = source[i][0];
            double ay = source[i][1];
            double bx = source[(i + 1) % n][0];
            double by = source[(i + 1) % n][1];

            // Normal tegak lurus terhadap edge (bx-ax, by-ay)
            double nx = -(by - ay);
            double ny = bx - ax;

            double min1 = Double.MAX_VALUE, max1 = -Double.MAX_VALUE;
            double min2 = Double.MAX_VALUE, max2 = -Double.MAX_VALUE;

            for (double[] p : source) {
                double proj = p[0] * nx + p[1] * ny;
                if (proj < min1) {
                    min1 = proj;
                }
                if (proj > max1) {
                    max1 = proj;
                }
            }
            for (double[] p : target) {
                double proj = p[0] * nx + p[1] * ny;
                if (proj < min2) {
                    min2 = proj;
                }
                if (proj > max2) {
                    max2 = proj;
                }
            }

            // Strict inequality: menyentuh di tepian (max1 == min2) dianggap TIDAK overlap
            if (max1 <= min2 || max2 <= min1) {
                return true; // separating axis ditemukan
            }
        }
        return false; // tidak ada separating axis dari edges ini
    }
}
