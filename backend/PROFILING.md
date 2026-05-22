# Profiling: N+1 Query Problem pada Dashboard Kebun

## Latar Belakang

Endpoint `GET /api/kebun/dashboard` mengembalikan daftar kebun beserta jumlah Mandor aktif
dan jumlah Supir aktif di setiap kebun. Implementasi pertama (naif) menggunakan pendekatan
N+1: satu query untuk mengambil semua kebun, lalu dua query terpisah per-kebun untuk
menghitung assignment aktif.

## Identifikasi Masalah

Dengan N kebun di database, implementasi naif menjalankan **2N + 1 query**:

```
Query 1  : SELECT * FROM kebun                              → 1 query
Query 2  : SELECT COUNT(*) FROM mandor_assignment WHERE kebun_id = 1 AND unassigned_at IS NULL
Query 3  : SELECT COUNT(*) FROM driver_assignment WHERE kebun_id = 1 AND unassigned_at IS NULL
Query 4  : SELECT COUNT(*) FROM mandor_assignment WHERE kebun_id = 2 AND unassigned_at IS NULL
Query 5  : SELECT COUNT(*) FROM driver_assignment WHERE kebun_id = 2 AND unassigned_at IS NULL
...
Query 2N+1 : SELECT COUNT(*) FROM driver_assignment WHERE kebun_id = N AND unassigned_at IS NULL
```

Untuk 55 kebun → **111 query** per request.

Kode naif di `KebunServiceImpl.getDashboard(naive=true)`:

```java
List<Kebun> kebunList = kebunRepository.findAll();        // 1 query
result = kebunList.stream().map(k -> KebunDashboardItem.builder()
    .countMandorAktif(
        mandorAssignmentRepository.countByKebunIdAndUnassignedAtIsNull(k.getId())  // N query
    )
    .countSupirAktif(
        driverAssignmentRepository.countByKebunIdAndUnassignedAtIsNull(k.getId())  // N query
    )
    .build()
).collect(Collectors.toList());
```

## Solusi

Ganti ke satu JPQL query dengan `LEFT JOIN` dan `COUNT(DISTINCT ...)` yang di-`GROUP BY` kebun.
Total query turun dari **2N+1 menjadi 1**.

Query optimized di `KebunRepository.findDashboardOptimized()`:

```jpql
SELECT new com.b1.mysawit.kebun.dto.KebunDashboardItem(
    k.id, k.kodeKebun, k.namaKebun, k.luasHektare,
    COUNT(DISTINCT ma.id), COUNT(DISTINCT da.id)
)
FROM Kebun k
LEFT JOIN MandorAssignment ma ON ma.kebun = k AND ma.unassignedAt IS NULL
LEFT JOIN DriverAssignment da ON da.kebun = k AND da.unassignedAt IS NULL
GROUP BY k.id, k.kodeKebun, k.namaKebun, k.luasHektare
ORDER BY k.id
```

## Pengukuran

**Setup:**
- Database: PostgreSQL 17.10 (NeonDB, region ap-southeast-1)
- Data: 55 kebun, 55 mandor_assignment aktif, 105 driver_assignment aktif
- Endpoint: `GET /api/kebun/dashboard?naive={true|false}`
- Timing dicatat via `System.currentTimeMillis()` di `KebunServiceImpl`

**Hasil (5 run masing-masing):**

| Run | Naif (ms) | Optimized (ms) |
|-----|-----------|----------------|
| 1   | 3248      | 38             |
| 2   | 3219      | 57             |
| 3   | 2974      | 28             |
| 4   | 2961      | 28             |
| 5   | 2967      | 28             |
| **Rata-rata** | **3074** | **36** |

**Improvement: ~98.8% lebih cepat** (dari ~3 detik ke ~36ms).

Log output (dari `KebunServiceImpl`):
```
[PROFILING] getDashboard strategy=naive     kebun=55 elapsed=3248ms
[PROFILING] getDashboard strategy=optimized kebun=55 elapsed=38ms
```

## Kesimpulan

| Aspek              | Naif (N+1)     | Optimized      |
|--------------------|----------------|----------------|
| Jumlah query       | 2N+1 = 111     | 1              |
| Latency rata-rata  | ~3074 ms       | ~36 ms         |
| Skalabilitas       | O(N)           | O(1)           |

Pendekatan JOIN + GROUP BY lebih efisien karena database dapat memanfaatkan index pada
`kebun_id` dan `unassigned_at` dalam satu execution plan, dibanding round-trip network
ke database sebanyak 111 kali.
