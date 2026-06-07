# Dokumentasi API — Modul Manajemen Kebun Sawit

**Base URL:** `/api/kebun`  
**Otorisasi:** Semua endpoint memerlukan autentikasi dan role `Admin`.  
**Format:** JSON (Content-Type: application/json)

---

## Daftar Endpoint

| Method | Path | Deskripsi |
|--------|------|-----------|
| POST | `/api/kebun` | Buat kebun baru |
| GET | `/api/kebun` | List semua kebun (dengan filter) |
| GET | `/api/kebun/{id}` | Detail kebun by ID |
| GET | `/api/kebun/{id}/detail` | Detail kebun + Mandor + daftar Supir |
| PUT | `/api/kebun/{id}` | Update kebun |
| DELETE | `/api/kebun/{id}` | Hapus kebun |
| POST | `/api/kebun/assign-mandor` | Tugaskan Mandor ke kebun |
| POST | `/api/kebun/reassign-mandor` | Pindahkan Mandor ke kebun lain |
| POST | `/api/kebun/assign-supir` | Tugaskan Supir ke kebun |
| POST | `/api/kebun/reassign-supir` | Pindahkan Supir ke kebun lain |
| GET | `/api/kebun/dashboard` | Dashboard ringkasan kebun |

---

## 1. Buat Kebun Baru

**POST** `/api/kebun`

**Request Body:**
```json
{
  "kodeKebun": "KB-001",
  "namaKebun": "Kebun Sawit Alpha",
  "koordinat": "[(0,0),(200,0),(200,200),(0,200)]"
}
```

| Field | Tipe | Wajib | Keterangan |
|-------|------|-------|------------|
| kodeKebun | String | ✓ | Kode unik, tidak bisa diubah setelah dibuat |
| namaKebun | String | ✓ | Nama kebun |
| koordinat | String | ✓ | 4 titik sudut persegi sejajar sumbu koordinat |

**Aturan koordinat:**
- Format: `[(x1,y1),(x2,y2),(x3,y3),(x4,y4)]`
- Harus tepat 4 titik
- Harus membentuk **persegi** (axis-aligned, semua sisi sama panjang)
- Luas dikalkulasi otomatis dari koordinat (tidak perlu input manual)
- Koordinat tidak boleh tumpang tindih dengan kebun yang sudah ada

**Response 201:**
```json
{
  "id": 1,
  "kodeKebun": "KB-001",
  "namaKebun": "Kebun Sawit Alpha",
  "luasHektare": 4.00,
  "koordinat": "[(0,0),(200,0),(200,200),(0,200)]",
  "createdAt": "2026-05-22T10:00:00+07:00",
  "updatedAt": "2026-05-22T10:00:00+07:00"
}
```

**Error:**
| Status | Kondisi |
|--------|---------|
| 400 | Field wajib kosong, koordinat tidak valid, atau bukan persegi |
| 409 | Kode kebun sudah terdaftar |
| 422 | Koordinat tumpang tindih dengan kebun lain |

---

## 2. List Kebun

**GET** `/api/kebun?nama=&kode=`

**Query Parameters (opsional):**
| Parameter | Keterangan |
|-----------|------------|
| nama | Filter contains by nama kebun (case-insensitive) |
| kode | Filter contains by kode kebun (case-insensitive) |

**Response 200:**
```json
[
  {
    "id": 1,
    "kodeKebun": "KB-001",
    "namaKebun": "Kebun Sawit Alpha",
    "luasHektare": 4.00,
    "koordinat": "[(0,0),(200,0),(200,200),(0,200)]",
    "createdAt": "2026-05-22T10:00:00+07:00",
    "updatedAt": null
  }
]
```

---

## 3. Detail Kebun

**GET** `/api/kebun/{id}`

Response sama dengan item dalam list kebun.

**Error:** `404` jika kebun tidak ditemukan.

---

## 4. Detail Kebun + Mandor + Supir

**GET** `/api/kebun/{id}/detail?searchNamaSupir=`

**Query Parameters (opsional):**
| Parameter | Keterangan |
|-----------|------------|
| searchNamaSupir | Filter daftar supir by nama (case-insensitive) |

**Response 200:**
```json
{
  "id": 1,
  "kodeKebun": "KB-001",
  "namaKebun": "Kebun Sawit Alpha",
  "luasHektare": 4.00,
  "koordinat": "[(0,0),(200,0),(200,200),(0,200)]",
  "createdAt": "2026-05-22T10:00:00+07:00",
  "updatedAt": null,
  "mandor": {
    "id": 10,
    "nama": "Budi Santoso",
    "email": "budi@mysawit.com"
  },
  "supirList": [
    {
      "id": 20,
      "nama": "Andi Supir",
      "email": "andi@mysawit.com"
    }
  ]
}
```

`mandor` bernilai `null` jika belum ada Mandor yang ditugaskan.  
`supirList` bernilai `[]` jika belum ada Supir.

---

## 5. Update Kebun

**PUT** `/api/kebun/{id}`

**Request Body (semua field opsional):**
```json
{
  "namaKebun": "Kebun Sawit Alpha Updated",
  "koordinat": "[(0,0),(300,0),(300,300),(0,300)]"
}
```

- `kodeKebun` tidak bisa diubah.
- Jika `koordinat` diubah, luas dikalkulasi ulang otomatis dan validasi overlap dijalankan.

**Response 200:** KebunResponse dengan data terbaru.

**Error:**
| Status | Kondisi |
|--------|---------|
| 400 | Koordinat tidak valid atau bukan persegi |
| 404 | Kebun tidak ditemukan |
| 422 | Koordinat baru tumpang tindih dengan kebun lain |

---

## 6. Hapus Kebun

**DELETE** `/api/kebun/{id}`

**Response 204:** No content.

**Error:**
| Status | Kondisi |
|--------|---------|
| 404 | Kebun tidak ditemukan |
| 422 | Kebun masih terikat dengan Mandor aktif — copot Mandor terlebih dahulu |

---

## 7. Tugaskan Mandor ke Kebun

**POST** `/api/kebun/assign-mandor`

```json
{
  "mandorId": 10,
  "kebunId": 1
}
```

**Aturan:**
- User harus berole `Mandor`
- Mandor tidak boleh sedang aktif di kebun lain (1 Mandor = 1 kebun aktif)

**Response 204:** No content.

**Error:**
| Status | Kondisi |
|--------|---------|
| 404 | Mandor atau kebun tidak ditemukan |
| 422 | Mandor sudah aktif di kebun lain |

---

## 8. Pindahkan Mandor ke Kebun Lain (Reassign)

**POST** `/api/kebun/reassign-mandor`

```json
{
  "mandorId": 10,
  "oldKebunId": 1,
  "newKebunId": 2
}
```

**Aturan:**
- Mandor harus sedang aktif di `oldKebunId`
- Operasi atomik: copot dari kebun lama + assign ke kebun baru dalam satu transaksi

**Response 204:** No content.

---

## 9. Tugaskan Supir ke Kebun

**POST** `/api/kebun/assign-supir`

```json
{
  "supirId": 20,
  "kebunId": 1
}
```

**Aturan:**
- User harus berole `Supir`
- Supir tidak boleh sedang aktif di kebun lain (1 Supir = 1 kebun aktif)

**Response 204:** No content.

**Error:**
| Status | Kondisi |
|--------|---------|
| 404 | Supir atau kebun tidak ditemukan |
| 422 | Supir sudah aktif di kebun lain |

---

## 10. Pindahkan Supir ke Kebun Lain (Reassign)

**POST** `/api/kebun/reassign-supir`

```json
{
  "supirId": 20,
  "oldKebunId": 1,
  "newKebunId": 2
}
```

**Aturan:**
- Supir harus sedang aktif di `oldKebunId`
- Operasi atomik: copot dari kebun lama + assign ke kebun baru

**Response 204:** No content.

---

## 11. Dashboard Kebun

**GET** `/api/kebun/dashboard?naive=false`

Mengembalikan ringkasan tiap kebun beserta jumlah Mandor dan Supir aktif.

| Parameter | Default | Keterangan |
|-----------|---------|------------|
| naive | false | `true` = N+1 query (untuk demo profiling), `false` = optimized single query |

**Response 200:**
```json
[
  {
    "id": 1,
    "kodeKebun": "KB-001",
    "namaKebun": "Kebun Sawit Alpha",
    "luasHektare": 4.00,
    "countMandorAktif": 1,
    "countSupirAktif": 3
  }
]
```

---

## Business Rules

### Validasi Koordinat
- Kebun diasumsikan berbentuk **persegi** (bukan persegi panjang)
- Koordinat harus **axis-aligned** (sejajar sumbu X dan Y)
- 4 titik dengan tepat 2 nilai X unik dan 2 nilai Y unik
- Lebar dan tinggi harus sama (toleransi 1%)
- **Luas dikalkulasi otomatis**: `(maxX - minX) × (maxY - minY) ÷ 10.000` (dalam hektare)

### Validasi Overlap
- Menggunakan algoritma **Separating Axis Theorem (SAT)**
- Kebun yang bersinggungan di sisi (adjacent) dianggap **tidak overlap**
- Kebun yang benar-benar tumpang tindih → error 422

### Constraint Assignment
| Constraint | Keterangan |
|------------|------------|
| 1 Mandor = 1 Kebun | Mandor hanya bisa aktif di satu kebun pada satu waktu |
| 1 Supir = 1 Kebun | Supir hanya bisa aktif di satu kebun pada satu waktu |
| Reassign wajib langsung | Saat copot Mandor/Supir, harus langsung assign ke kebun lain (atomic) |
| Delete check | Kebun tidak bisa dihapus jika masih ada Mandor aktif |

---

## Keterkaitan dengan Modul Lain

### Modul Panen (Harvest)
- Mandor hanya bisa menyetujui/menolak hasil panen jika sudah ditempatkan di kebun
- Validasi dilakukan via WorkerAssignment: Buruh harus terhubung ke Mandor yang sama

### Modul Pengiriman (Delivery)
- Saat membuat delivery, sistem memvalidasi bahwa **Mandor dan Supir berada di kebun yang sama**
- Validasi via `KebunAssignmentFacade.validateSameKebun(mandorId, supirId)`
- Error 422 jika Mandor/Supir belum di kebun atau berbeda kebun

---

## Error Response Format

Semua error mengikuti format standar Spring Boot:

```json
{
  "timestamp": "2026-05-22T10:00:00+07:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Koordinat kebun tumpang tindih dengan kebun yang sudah ada (id: 2)"
}
```

| Status | Exception | Kondisi umum |
|--------|-----------|--------------|
| 400 | IllegalArgumentException | Input tidak valid |
| 401 | - | Tidak terautentikasi |
| 403 | AccessDeniedException | Bukan role Admin |
| 404 | ResourceNotFoundException | Resource tidak ditemukan |
| 409 | DuplicateResourceException | Kode kebun duplikat |
| 422 | BusinessRuleViolationException | Business rule terlanggar |
