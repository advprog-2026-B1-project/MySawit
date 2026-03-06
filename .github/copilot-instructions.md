# Copilot Instructions — MySawit

## Project Overview

MySawit is a web-based system used to manage palm oil plantation operations, including worker coordination, harvest reporting, and shipment logistics.

The system manages several operational roles:

* Admin
* Buruh (harvest workers)
* Mandor (supervisors)
* Supir (truck drivers)

The application is implemented as a **Modular Monolith** where each feature is organized into a module but runs inside a single Spring Boot application.

---

# Technology Stack

Frontend

* NextJS
* Tailwind CSS

Backend

* Java 21
* Spring Boot
* Spring Security
* BCrypt
* Gradle

Database

* PostgreSQL

Tools

* Docker (local database)
* GitHub Actions CI
* SonarCloud
* JaCoCo
* ESLint
* k6 performance testing

---

# Architecture Rules

The backend follows **modular monolith architecture**.

Each module should have:

* controller
* service
* repository
* entity
* dto

General rules:

Controllers

* handle HTTP requests
* call service layer
* return DTO responses

Services

* contain business logic
* coordinate repositories
* enforce validation rules

Repositories

* extend `JpaRepository`
* contain database queries

Entities

* map directly to database tables
* should not contain business logic

DTOs

* used for API request/response
* prevent exposing entities directly

Constructor injection should be preferred.

---

# System Roles

Role values stored in database:

* Admin
* Buruh
* Mandor
* Supir

Role-based access must be enforced using **Spring Security**.

Endpoint access patterns should follow:

```
/admin/**
/buruh/**
/mandor/**
/supir/**
```

---

# Module 1 — Authentication, Authorization, and User Management

Responsible for managing system users and authentication.

Main entity: `User`

Database fields:

* id
* username
* email
* nama
* password_hash
* role
* saldo
* created_at
* updated_at

Mandor-specific data stored in:

`mandor_detail`

Fields:

* user_id
* nomor_sertifikasi

### Features

User Registration

* register new user
* validate unique email
* hash password using BCrypt
* assign role

Login

* authenticate using email and password
* verify BCrypt hash
* generate authentication token or session

User Management (Admin)

Admin can:

* list users
* filter users by role
* search by name or email
* delete users
* update user information

Worker Assignment

Admin can assign workers to Mandor.

Uses table:

`worker_assignment`

Fields:

* worker_id
* mandor_id
* assigned_at
* unassigned_at

Rules:

* worker can only have one active mandor
* assignment history should be preserved

---

# Module 2 — Plantation Management

Responsible for managing plantation (kebun) information.

Main entity: `Kebun`

Fields:

* id
* kode_kebun
* nama_kebun
* luas_hektare
* koordinat (JSON)
* created_at
* updated_at

Coordinates represent **four corner points** of the plantation.

### Rules

* plantations must be square-shaped
* plantations cannot overlap
* `kode_kebun` cannot be modified after creation

### Admin Features

Create Plantation

* validate unique `kode_kebun`
* validate coordinate format

Update Plantation

* allow changing name, area, coordinates
* prevent changing plantation code

Delete Plantation

* only if not currently assigned

### Assignments

Mandor Assignment

Table:

`mandor_assignment`

Fields:

* mandor_id
* kebun_id
* assigned_at
* unassigned_at

Driver Assignment

Table:

`driver_assignment`

Fields:

* driver_id
* kebun_id
* assigned_at
* unassigned_at

Rules:

* only Admin can assign
* Mandor and Supir must be assigned before performing operational tasks

---

# Module 3 — Harvest Management

Responsible for recording harvest results submitted by workers.

Main entity: `HasilPanen`

Fields:

* worker_id
* kebun_id
* tanggal_panen
* kilogram
* berita
* status
* rejection_reason
* created_at
* updated_at

Status values:

* Pending
* Approved
* Rejected

### Harvest Submission

Workers can submit harvest reports containing:

* harvest date
* harvested weight (kg)
* harvest report text
* photos

Photos stored in table:

`foto_hasil_panen`

Fields:

* harvest_id
* url
* uploaded_at

### Rules

Workers:

* can submit **only one harvest report per day**
* cannot modify reports after submission

Mandor:

* reviews submitted reports
* can approve or reject
* must provide rejection reason if rejected

Rejected harvest reports cannot proceed to shipment.

---

# Module 4 — Harvest Shipment Management

Responsible for transporting harvest results from plantation to processing facility.

Main entity: `Delivery`

Fields:

* driver_id
* mandor_id
* harvest_id
* status
* rejected_kg
* rejection_reason
* created_at
* updated_at

Shipment status flow:

```
Memuat → Mengirim → Tiba
```

### Responsibilities

Mandor

* assign harvest shipments to drivers
* monitor shipment progress
* reject shipment if necessary

Driver

* view assigned deliveries
* update delivery status
* mark shipment progress

### Rules

* only **Approved harvest** can be shipped
* shipment must not exceed **400kg truck capacity**
* shipment status must follow defined order

---

# Wallet System (Fake Money)

The system does **not integrate real payment gateways**.

Instead it uses an internal fake wallet.

Wallet balance stored in:

```
users.saldo
```

Payroll operations increase wallet balance after approval.

No external financial APIs should be implemented.

---

# Validation Requirements

Code generated must enforce:

* unique email addresses
* worker harvest once per day
* shipment capacity ≤ 400kg
* correct role authorization
* required fields validation

---

# Performance Requirements

System must support:

* 50 concurrent users
* read endpoint response time < 500ms
* API error rate < 1%

Prefer:

* indexed queries
* pagination for large lists
* avoiding N+1 queries
* efficient joins


Approximate folder structure:

backend/
│
├─ src/
│  ├─ main/
│  │
│  │  ├─ java/com/mysawit/
│  │  │
│  │  │  ├─ MySawitApplication.java
│  │  │
│  │  │  ├─ config/
│  │  │  │   ├─ SecurityConfig.java
│  │  │  │   ├─ JwtConfig.java
│  │  │  │   └─ DatabaseConfig.java
│  │  │
│  │  │  ├─ common/
│  │  │  │   ├─ exception/
│  │  │  │   │   ├─ GlobalExceptionHandler.java
│  │  │  │   │   └─ NotFoundException.java
│  │  │  │   ├─ response/
│  │  │  │   │   └─ ApiResponse.java
│  │  │  │   └─ util/
│  │  │
│  │  │  ├─ entity/               ← ALL JPA entities live here
│  │  │  │
│  │  │  │   ├─ User.java
│  │  │  │   ├─ MandorDetail.java
│  │  │  │
│  │  │  │   ├─ Kebun.java
│  │  │  │   ├─ MandorAssignment.java
│  │  │  │   ├─ DriverAssignment.java
│  │  │  │   ├─ WorkerAssignment.java
│  │  │  │
│  │  │  │   ├─ Harvest.java
│  │  │  │   ├─ HarvestPhoto.java
│  │  │  │
│  │  │  │   └─ Delivery.java
│  │  │
│  │  │  ├─ repository/           ← Spring Data JPA interfaces
│  │  │  │
│  │  │  │   ├─ UserRepository.java
│  │  │  │   ├─ KebunRepository.java
│  │  │  │   ├─ HarvestRepository.java
│  │  │  │   ├─ HarvestPhotoRepository.java
│  │  │  │   └─ DeliveryRepository.java
│  │  │
│  │  │  ├─ auth/                 ← Module 1
│  │  │  │
│  │  │  │   ├─ controller/
│  │  │  │   │   └─ AuthController.java
│  │  │  │   │
│  │  │  │   ├─ service/
│  │  │  │   │   └─ AuthService.java
│  │  │  │   │
│  │  │  │   └─ dto/
│  │  │  │       ├─ LoginRequest.java
│  │  │  │       ├─ RegisterRequest.java
│  │  │  │       └─ UserResponse.java
│  │  │
│  │  │  ├─ kebun/                ← Module 2
│  │  │  │
│  │  │  │   ├─ controller/
│  │  │  │   │   └─ KebunController.java
│  │  │  │   │
│  │  │  │   ├─ service/
│  │  │  │   │   └─ KebunService.java
│  │  │  │   │
│  │  │  │   └─ dto/
│  │  │  │       ├─ CreateKebunRequest.java
│  │  │  │       └─ KebunResponse.java
│  │  │
│  │  │  ├─ panen/                ← Module 3
│  │  │  │
│  │  │  │   ├─ controller/
│  │  │  │   │   └─ HarvestController.java
│  │  │  │   │
│  │  │  │   ├─ service/
│  │  │  │   │   └─ HarvestService.java
│  │  │  │   │
│  │  │  │   └─ dto/
│  │  │  │       ├─ SubmitHarvestRequest.java
│  │  │  │       └─ HarvestResponse.java
│  │  │
│  │  │  ├─ delivery/             ← Module 4 (your module)
│  │  │  │
│  │  │  │   ├─ controller/
│  │  │  │   │   └─ DeliveryController.java
│  │  │  │   │
│  │  │  │   ├─ service/
│  │  │  │   │   └─ DeliveryService.java
│  │  │  │   │
│  │  │  │   └─ dto/
│  │  │  │       ├─ StartDeliveryRequest.java
│  │  │  │       └─ DeliveryResponse.java
│  │  │
│  │  │
│  │  └─ resources/
│  │
│  │      ├─ application.yml
│  │      ├─ application-test.yml
│  │      │
│  │      └─ db/
│  │          └─ migration/        ← if using Flyway
│  │
│  └─ test/
│
│     └─ java/com/mysawit/
│
│         ├─ auth/
│         │   └─ AuthServiceTest.java
│         │
│         ├─ kebun/
│         │   └─ KebunServiceTest.java
│         │
│         ├─ panen/
│         │   └─ HarvestServiceTest.java
│         │
│         └─ delivery/
│             └─ DeliveryServiceTest.java
│
├─ build.gradle
├─ gradlew
├─ gradlew.bat
└─ settings.gradle
