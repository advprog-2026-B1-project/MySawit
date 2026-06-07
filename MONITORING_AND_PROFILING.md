# Monitoring dan Profiling — MySawit (Modul Kebun)

---

## 1. Monitoring

### Link Commit

| Commit | Deskripsi |
|--------|-----------|
| [`5f20404`](https://github.com/advprog-2026-B1-project/MySawit/commit/5f20404) | Add Spring Boot Actuator — expose `/actuator/prometheus`, `/actuator/health` |
| [`d6d6422`](https://github.com/advprog-2026-B1-project/MySawit/commit/d6d6422) | Add Grafana provisioning + MySawit monitoring dashboard (10 panel) |
| [`cdea0c5`](https://github.com/advprog-2026-B1-project/MySawit/commit/cdea0c5) | Update Prometheus config — tambah scrape target production EC2 |

### Justifikasi Desain

Stack monitoring yang dipilih: **Spring Boot Actuator → Prometheus → Grafana**.

1. **Spring Boot Actuator** — terintegrasi langsung dengan aplikasi tanpa instrumentasi tambahan. Dengan `micrometer-registry-prometheus`, semua metric JVM, HTTP, dan HikariCP otomatis tersedia di `/actuator/prometheus`.

2. **Prometheus** — scrape metric secara pull-based setiap 5 detik. Model pull lebih aman untuk deployment di belakang NAT/firewall, dan menyimpan time-series data untuk query histori.

3. **Grafana** — visualisasi panel fleksibel dengan PromQL. Auto-provisioning via YAML + JSON sehingga dashboard langsung muncul tanpa setup manual saat `docker compose up`.

Metric yang dipantau mencakup HTTP request rate, P95 response time, error rate (4xx+5xx), JVM memory, HikariCP connection pool, dan CPU usage — cukup untuk mendeteksi regresi performa dan bottleneck DB sejak dini.

### Contoh Penggunaan

Verifikasi metric tersedia:
```bash
curl http://localhost:8080/actuator/prometheus | grep http_server_requests
```

Query PromQL untuk P95 latency endpoint kebun di Grafana:
```promql
histogram_quantile(0.95,
  rate(http_server_requests_seconds_bucket{uri=~"/api/kebun.*"}[5m])
)
```

---

## 2. Profiling

### Link Commit

| Commit | Deskripsi |
|--------|-----------|
| [`fcd0fcb`](https://github.com/advprog-2026-B1-project/MySawit/commit/fcd0fcb) | Implement optimized dashboard query — resolve N+1 problem, tambah `PROFILING.md` |

Dokumen profiling lengkap: [`backend/PROFILING.md`](backend/PROFILING.md).

### Justifikasi Proses Profiling

Target: endpoint `GET /api/kebun/dashboard` — dipilih karena mengagregasi data dari tiga tabel (kebun, mandor_assignment, driver_assignment), rentan N+1 query.

1. **Hibernate SQL logging** — aktifkan `show-sql: true` + `format_sql: true` di `application.yml` untuk melihat setiap query ke DB. Dari sini terlihat bahwa 55 kebun menghasilkan 111 query per request.

2. **Timer manual di service layer** — `System.currentTimeMillis()` sebelum dan sesudah eksekusi, log via `System.out.printf`. Non-invasif, reprodusibel tanpa profiler eksternal.

3. **Toggle `?naive=true|false`** — perbandingan dilakukan di endpoint yang sama dengan kondisi DB yang sama, tanpa restart.

4. **Kenapa tidak pakai VisualVM/async-profiler** — masalah N+1 ada di network I/O ke DB, bukan CPU. Profiler sampling CPU tidak akan menangkap ini; SQL logging + timer lebih tepat sasaran.

### Analisis Improvement

Implementasi naif menjalankan **2N + 1 query** (111 query untuk 55 kebun). Setelah diganti ke single JPQL query dengan `LEFT JOIN` + `GROUP BY`, jumlah query turun menjadi **1**.

| | Naif (N+1) | Optimized |
|---|---|---|
| Jumlah query | 111 | 1 |
| Latency rata-rata | ~3074 ms | ~36 ms |
| Skalabilitas | O(N) | O(1) |

**Improvement: ~98.8% lebih cepat.** Database memanfaatkan index pada `kebun_id` dan `unassigned_at` dalam satu execution plan, dibanding 111 round-trip ke DB.
