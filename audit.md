# Laporan Audit Codebase

**Project:** RSIS (Rumah Sakit Information System)
**Audit Date:** 2025-01-18
**Auditor:** Cascade AI

---

## Ringkasan

| Kategori | Total Masalah Ditemukan |
|---|---|
| Pelanggaran MVC | 8 |
| Pelanggaran OOP | 5 |
| Logic Salah Tempat | 10 |
| **Total** | **23** |

---

## 1. Pelanggaran MVC

### [CRITICAL] PasienController — Direct repository access di controller
- **File:** `src/main/java/rsis/controller/PasienController.java`
- **Method:** `dashboard()`, `jadwalRiwayat()`
- **Issue:** Controller memanggil repository secara langsung melalui service getter (`appointmentService.getAppointmentRepository().save()`) dan melakukan update database. Ini melanggar prinsip MVC di mana controller seharusnya hanya menangani HTTP request/response dan tidak boleh mengakses repository secara langsung.
- **Rekomendasi:** Pindahkan logic auto-update status appointment ke AppointmentService. Buat method baru seperti `updateExpiredAppointments()` di service layer yang dipanggil oleh controller.

### [CRITICAL] PasienController — Business logic di controller
- **File:** `src/main/java/rsis/controller/PasienController.java`
- **Method:** `dashboard()` (lines 131-142), `jadwalRiwayat()` (lines 359-370), `getAppointmentDetail()` (lines 491-500), `getJadwalByDokter()` (lines 518-529)
- **Issue:** Controller melakukan manual population transient fields untuk entity Dokter dengan meng-query UserRepository. Ini adalah business logic yang seharusnya ada di service layer.
- **Rekomendasi:** Pindahkan logic population transient field ke service layer atau buat shared utility method yang dapat digunakan oleh semua service yang membutuhkan.

### [CRITICAL] PasienController — Auto-update appointment status logic di controller
- **File:** `src/main/java/rsis/controller/PasienController.java`
- **Method:** `dashboard()` (lines 112-129), `jadwalRiwayat()` (lines 340-357)
- **Issue:** Controller melakukan date comparison dan update status appointment secara manual. Logic ini adalah business rule yang seharusnya ada di service layer.
- **Rekomendasi:** Buat method di AppointmentService untuk menangani auto-update status berdasarkan tanggal. Method ini dapat dipanggil oleh controller atau dijadwalkan sebagai scheduled task.

### [WARNING] DokterController — Complex data structure building di controller
- **File:** `src/main/java/rsis/controller/DokterController.java`
- **Method:** `getJadwalDokter()` (lines 323-388)
- **Issue:** Controller membangun struktur data kompleks (jadwalWithDates) dengan business logic untuk mapping appointment ke jadwal. Ini seharusnya ada di service layer.
- **Rekomendasi:** Pindahkan logic pembuatan struktur data ini ke DokterService. Service harus mengembalikan DTO yang sudah siap untuk dikonsumsi oleh view.

### [WARNING] AdminController — Business logic di controller
- **File:** `src/main/java/rsis/controller/AdminController.java`
- **Method:** `addJadwalStatsToModel()` (lines 66-90)
- **Issue:** Method ini menghitung statistik jadwal (count tersedia, penuh, libur) yang merupakan business logic. Controller seharusnya hanya menerima data dari service.
- **Rekomendasi:** Pindahkan logic perhitungan statistik ke AdminRSService dan buat method yang mengembalikan object statistik.

### [WARNING] AdminController — Helper method di controller
- **File:** `src/main/java/rsis/controller/AdminController.java`
- **Method:** `safeDashboardValue()` (lines 163-170)
- **Issue:** Method ini adalah helper untuk error handling yang seharusnya ada di utility class atau service layer, bukan di controller.
- **Rekomendasi:** Pindahkan method ini ke utility class atau gunakan exception handling yang lebih terstruktur di service layer.

### [INFO] AuthController — Debug endpoint di production code
- **File:** `src/main/java/rsis/controller/AuthController.java`
- **Method:** `tempDebug()` (lines 34-74)
- **Issue:** Endpoint debug ini seharusnya tidak ada di production code. Ini adalah security risk dan melanggar best practice.
- **Rekomendasi:** Hapus endpoint debug ini. Untuk debugging, gunakan logging atau debugging tools yang proper.

### [INFO] Model classes — Business logic di entity classes
- **File:** `src/main/java/rsis/model/Appointment.java`, `JadwalPraktik.java`
- **Method:** `batalkan()`, `konfirmasi()`, `tolak()` di Appointment; `cekTersedia()`, `tambahKuota()`, `kurangiKuota()` di JadwalPraktik
- **Issue:** Entity classes mengandung business logic yang kompleks. Ini adalah gray area antara rich domain model dan anemic domain model, namun dapat dianggap pelanggaran MVC jika logic terlalu kompleks.
- **Rekomendasi:** Pertimbangkan untuk memindahkan logic yang kompleks ke service layer, atau gunakan pattern seperti Domain Service jika ingin mempertahankan rich domain model.

---

## 2. Pelanggaran OOP

### [CRITICAL] AdminRSService — God class (SRP violation)
- **File:** `src/main/java/rsis/service/AdminRSService.java`
- **Principle Violated:** Single Responsibility Principle (SRP)
- **Issue:** Class ini menangani terlalu banyak tanggung jawab: dokter management, poli management, spesialisasi management, jadwal management, file upload, ID generation, statistics/reporting, dan day translation. Ini adalah god class yang sulit di-maintain dan test.
- **Rekomendasi:** Pecah AdminRSService menjadi multiple service classes yang lebih spesifik: DokterManagementService, PoliManagementService, JadwalManagementService, StatisticsService, FileStorageService, IdGeneratorService, dll.

### [WARNING] PasienController — Multiple responsibilities (SRP violation)
- **File:** `src/main/java/rsis/controller/PasienController.java`
- **Principle Violated:** Single Responsibility Principle (SRP)
- **Issue:** Controller ini menangani dashboard, profile management, search doctors, booking, appointment history, dan appointment details. Terlalu banyak tanggung jawab dalam satu controller.
- **Rekomendasi:** Pertimbangkan untuk memecah controller ini menjadi multiple controllers: DashboardController, ProfileController, BookingController, AppointmentHistoryController.

### [WARNING] Hard-coded status strings (OCP violation)
- **File:** Multiple files across the codebase
- **Principle Violated:** Open/Closed Principle (OCP)
- **Issue:** Status strings seperti "MENUNGGU", "DIKONFIRMASI", "SELESAI", "DIBATALKAN", "DITOLAK" di-hard-coded di banyak tempat. Jika ingin menambah status baru atau mengubah nama, harus mengubah banyak file.
- **Rekomendasi:** Buat enum atau constant class untuk semua status values. Ini akan membuat code lebih maintainable dan extensible.

### [INFO] Anemic Domain Model
- **File:** `src/main/java/rsis/model/Pasien.java`, `Dokter.java`, `AdminRS.java`, `Poli.java`, `Spesialisasi.java`
- **Principle Violated:** Domain Model Design
- **Issue:** Entity classes memiliki placeholder methods dengan komentar "logic in services". Ini menunjukkan anemic domain model di mana semua business logic dipindahkan ke service layer, melanggar prinsip object-oriented design.
- **Rekomendasi:** Pertimbangkan untuk memindahkan beberapa business logic yang relevan ke entity classes (rich domain model) atau gunakan Domain Service pattern untuk logic yang kompleks.

### [INFO] Duplicate transient field population logic
- **File:** `src/main/java/rsis/service/AdminRSService.java`, `DokterService.java`, `PasienService.java`, `AppointmentService.java`
- **Principle Violated:** Don't Repeat Yourself (DRY)
- **Issue:** Logic untuk populate transient fields (nama, email, password, role) dari User table ke Dokter entity di-duplicate di multiple service classes.
- **Rekomendasi:** Ekstrak logic ini ke shared utility method atau service yang dapat digunakan oleh semua service yang membutuhkan.

---

## 3. Logic Salah Tempat

### PasienController#dashboard()
- **Current Location:** `rsis.controller.PasienController`
- **Issue:** Method ini mengandung logic auto-update appointment status berdasarkan tanggal (lines 112-129). Ini adalah business logic yang seharusnya ada di service layer.
- **Should Be In:** `rsis.service.AppointmentService`
- **Reason:** Auto-update status adalah business rule yang berkaitan dengan appointment lifecycle, bukan concern dari HTTP request handling.

### PasienController#jadwalRiwayat()
- **Current Location:** `rsis.controller.PasienController`
- **Issue:** Method ini mengandung logic auto-update appointment status yang sama dengan dashboard() (lines 340-357). Duplication logic yang seharusnya ada di service.
- **Should Be In:** `rsis.service.AppointmentService`
- **Reason:** Sama dengan di atas, auto-update status adalah business logic yang seharusnya terpusat di service layer.

### PasienController#dashboard(), jadwalRiwayat(), getAppointmentDetail(), getJadwalByDokter()
- **Current Location:** `rsis.controller.PasienController`
- **Issue:** Transient field population logic di-duplicate di multiple methods. Logic ini meng-query UserRepository untuk populate fields Dokter.
- **Should Be In:** `rsis.service.UserService` atau shared utility class
- **Reason:** Population transient field adalah data transformation logic yang seharusnya ada di service layer, bukan di controller.

### AdminController#addJadwalStatsToModel()
- **Current Location:** `rsis.controller.AdminController`
- **Issue:** Method ini menghitung statistik jadwal (count tersedia, penuh, libur) dan menambahkan ke model. Ini adalah business logic.
- **Should Be In:** `rsis.service.AdminRSService`
- **Reason:** Perhitungan statistik adalah business logic yang seharusnya ada di service layer. Controller seharusnya hanya menerima hasil dari service.

### AdminController#safeDashboardValue()
- **Current Location:** `rsis.controller.AdminController`
- **Issue:** Method ini adalah helper untuk error handling yang tidak seharusnya ada di controller.
- **Should Be In:** Utility class seperti `rsis.util.ExceptionHandlerUtil`
- **Reason:** Helper methods untuk error handling seharusnya ada di utility layer agar dapat digunakan secara reusable.

### DokterController#getJadwalDokter()
- **Current Location:** `rsis.controller.DokterController`
- **Issue:** Method ini membangun struktur data kompleks (jadwalWithDates) dengan mapping appointment ke jadwal. Ini adalah data transformation logic.
- **Should Be In:** `rsis.service.DokterService`
- **Reason:** Data transformation dan structure building adalah business logic yang seharusnya ada di service layer.

### AdminRSService#saveUploadedFile()
- **Current Location:** `rsis.service.AdminRSService`
- **Issue:** Method ini menangani file upload logic yang tidak berkaitan dengan admin RS domain logic.
- **Should Be In:** `rsis.service.FileStorageService`
- **Reason:** File storage adalah cross-cutting concern yang seharusnya ada di service terpisah agar dapat digunakan oleh multiple parts of application.

### AdminRSService#generateDokterId(), generateNomorStr(), generateJadwalId(), generatePoliId()
- **Current Location:** `rsis.service.AdminRSService`
- **Issue:** ID generation logic di-duplicate dan tersebar di multiple methods. Logic ini tidak spesifik untuk admin RS domain.
- **Should Be In:** `rsis.service.IdGeneratorService`
- **Reason:** ID generation adalah cross-cutting concern yang seharusnya terpusat di satu service agar konsisten dan reusable.

### AdminRSService#translateDayToIndonesian()
- **Current Location:** `rsis.service.AdminRSService`
- **Issue:** Method ini adalah utility untuk translate nama hari ke Bahasa Indonesia. Tidak seharusnya ada di service yang fokus pada admin RS domain.
- **Should Be In:** Utility class seperti `rsis.util.DateTimeUtil`
- **Reason:** Translation dan formatting adalah utility function yang seharusnya ada di utility layer.

### AuthController#tempDebug()
- **Current Location:** `rsis.controller.AuthController`
- **Issue:** Method ini adalah debug endpoint untuk testing yang tidak seharusnya ada di production code.
- **Should Be In:** Tidak ada (harus dihapus)
- **Reason:** Debug endpoints adalah security risk dan tidak seharusnya ada di production. Gunakan proper debugging tools atau logging.

---

## 4. Rekomendasi & Prioritas Refactoring

Berikut adalah prioritas perbaikan berdasarkan severity:

1. **[CRITICAL]** Pindahkan direct repository access dari PasienController ke AppointmentService
2. **[CRITICAL]** Pecah AdminRSService menjadi multiple service classes yang lebih spesifik (SRP)
3. **[CRITICAL]** Pindahkan auto-update appointment status logic dari controller ke service layer
4. **[CRITICAL]** Hapus debug endpoint tempDebug dari AuthController
5. **[WARNING]** Buat enum atau constant class untuk status values (OCP)
6. **[WARNING]** Ekstrak transient field population logic ke shared utility/service
7. **[WARNING]** Pindahkan statistics calculation logic dari AdminController ke AdminRSService
8. **[WARNING]** Pindahkan complex data structure building dari DokterController ke DokterService
9. **[INFO]** Buat FileStorageService terpisah untuk file upload logic
10. **[INFO]** Buat IdGeneratorService terpisah untuk ID generation logic
11. **[INFO]** Buat DateTimeUtil untuk translation dan formatting logic
12. **[INFO]** Pertimbangkan untuk memecah PasienController menjadi multiple controllers
13. **[INFO]** Review dan refactor entity classes untuk mengurangi anemic domain model

---

## Catatan Tambahan

- **Codebase secara umum sudah mengikuti struktur MVC dengan pemisahan yang jelas antara model, view, dan controller.**
- **Penggunaan Spring Boot dan JPA sudah tepat untuk aplikasi ini.**
- **Thymeleaf templates sudah bersih tanpa business logic yang embedded.**
- **Static resources (CSS, JS) sudah terorganisir dengan baik.**
- **Repository layer sudah bersih tanpa business logic.**
- **DTOs sudah digunakan dengan baik untuk data transfer.**
- **Security configuration sudah proper dengan role-based access control.**

Perbaikan yang disarankan di atas bertujuan untuk meningkatkan maintainability, testability, dan adherence ke best practices software architecture. Prioritas utama adalah menangani pelanggaran CRITICAL yang dapat menyebabkan masalah security atau architecture debt yang serius.
