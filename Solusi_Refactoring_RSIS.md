**RSIS**

Rumah Sakit Intelligent System

**PANDUAN SOLUSI REFACTORING**

Berdasarkan Hasil Audit MVC & OOP

| Proyek | RSIS Spring Boot |
| --- | --- |
| Dibuat oleh | Claude AI |
| Berdasarkan | audit.md + Class Diagram + DB Schema |
| Acuan Utama | Class_Diagram.svg |
| Total Temuan | 23 Issues (8 MVC + 5 OOP + 10 Logic) |

# **Ringkasan Eksekutif**

Dokumen ini merupakan panduan solusi konkret berdasarkan hasil audit codebase RSIS yang dilakukan oleh Cascade AI. Seluruh rekomendasi telah disesuaikan dengan class diagram sebagai acuan desain utama, struktur folder yang ada, serta skema database PostgreSQL.

| **Kategori** | **Ditemukan** | **Prioritas** | **Dampak** |
| --- | --- | --- | --- |
| Pelanggaran MVC | 8 issues | CRITICAL: 3, WARNING: 2, INFO: 3 | Arsitektur tidak bersih |
| Pelanggaran OOP | 5 issues | CRITICAL: 1, WARNING: 2, INFO: 2 | Maintainability buruk |
| Logic Salah Tempat | 10 issues | Semua perlu diperbaiki | Sulit dikembangkan |
| Total | 23 issues | - | Refactoring diperlukan |

# **Bab 1 — Solusi Pelanggaran MVC**

Berikut solusi untuk setiap pelanggaran MVC yang ditemukan, disesuaikan dengan class diagram dan service yang sudah ada.

## **1.1  [CRITICAL] PasienController — Direct Repository Access**

| **Masalah** Controller memanggil repository secara langsung dan melakukan update database. Melanggar prinsip MVC — Controller hanya boleh berkomunikasi dengan Service. |
| --- |

### **Solusi: Tambahkan method di AppointmentService**

Pindahkan semua logika auto-update status appointment ke dalam AppointmentService. Sesuai class diagram, Appointment memiliki method konfirmasi(), batalkan(), dan tolak() — logika status harus dikelola di service layer.

| // AppointmentService.java — tambahkan method baru public void updateExpiredAppointments() {     LocalDate today = LocalDate.now();     List<Appointment> appointments = appointmentRepository         .findByStatusAndTanggalBookingBefore("MENUNGGU", today);         for (Appointment apt : appointments) {         apt.setStatus("SELESAI");  // atau EXPIRED sesuai kebutuhan         appointmentRepository.save(apt);     } } // PasienController.java — cukup panggil service // SEBELUM (salah): // appointmentService.getAppointmentRepository().save(apt); // SESUDAH (benar): appointmentService.updateExpiredAppointments(); |
| --- |

## **1.2  [CRITICAL] PasienController — Business Logic di Controller**

| **Masalah** Transient field population logic (query UserRepository untuk populate field Dokter) dilakukan manual di dalam controller, bukan di service. |
| --- |

### **Solusi: Buat method helper di DokterService**

Sesuai class diagram, Dokter memiliki relasi ke User (via id_user di tabel dokter). Logika pengisian field transient harus ada di DokterService.

| // DokterService.java — tambahkan method populate public Dokter populateDokterFromUser(Dokter dokter) {     User user = userRepository.findById(dokter.getIdUser()).orElse(null);     if (user != null) {         dokter.setNama(user.getNama());         dokter.setEmail(user.getEmail());         // set field transient lainnya     }     return dokter; } public List<Dokter> populateAllDokterFromUser(List<Dokter> dokterList) {     return dokterList.stream()         .map(this::populateDokterFromUser)         .collect(Collectors.toList()); } // PasienController.java — hapus logika manual, ganti dengan: List<Dokter> dokterList = dokterService.populateAllDokterFromUser(rawList); |
| --- |

## **1.3  [WARNING] DokterController — Complex Data Structure di Controller**

| **Masalah** Method getJadwalDokter() membangun struktur data kompleks (jadwalWithDates) dengan mapping appointment ke jadwal langsung di controller. |
| --- |

### **Solusi: Buat DTO dan pindahkan ke JadwalPraktikService**

Sesuai class diagram, JadwalPraktik sudah memiliki relasi ke Dokter dan method getTanggal(). Gunakan JadwalDTO yang sudah ada di package dto.

| // JadwalPraktikService.java — tambahkan method public List<JadwalDTO> getJadwalWithDatesForDokter(String idDokter) {     List<JadwalPraktik> jadwalList = jadwalPraktikRepository         .findByDokter_IdDokter(idDokter);         return jadwalList.stream().map(jadwal -> {         JadwalDTO dto = new JadwalDTO();         dto.setIdJadwal(jadwal.getIdJadwalPraktik());         dto.setHari(jadwal.getHari());         dto.setTanggal(jadwal.getTanggal());         // mapping field lainnya...         return dto;     }).collect(Collectors.toList()); } // DokterController.java model.addAttribute("jadwalList",     jadwalPraktikService.getJadwalWithDatesForDokter(idDokter)); |
| --- |

## **1.4  [WARNING] AdminController — Business Logic ****&**** Helper di Controller**

### **Solusi A: Pindahkan statistik jadwal ke AdminRSService**

| // AdminRSService.java — tambahkan method statistik jadwal public Map<String, Long> getJadwalStatistics() {     List<JadwalPraktik> allJadwal = jadwalPraktikRepository.findAll();     Map<String, Long> stats = new HashMap<>();     stats.put("tersedia", allJadwal.stream()         .filter(j -> "TERSEDIA".equals(j.getStatusKetersediaan())).count());     stats.put("penuh", allJadwal.stream()         .filter(j -> "PENUH".equals(j.getStatusKetersediaan())).count());     stats.put("libur", allJadwal.stream()         .filter(j -> "LIBUR".equals(j.getStatusKetersediaan())).count());     return stats; } // AdminController.java Map<String, Long> jadwalStats = adminRSService.getJadwalStatistics(); model.addAttribute("jadwalStats", jadwalStats); |
| --- |

### **Solusi B: Hapus safeDashboardValue() — gunakan Optional**

| // SEBELUM (di controller — salah): private Object safeDashboardValue(Object value) { ... } // SESUDAH (pakai Optional langsung di service): public long getTotalPasienHariIni() {     try {         return appointmentRepository.countByTanggalBooking(LocalDate.now());     } catch (Exception e) {         log.error("Error getTotalPasienHariIni", e);         return 0L;     } } |
| --- |

## **1.5  [CRITICAL] AuthController — Hapus Debug Endpoint**

| **Perhatian Keamanan** **Method tempDebug() di AuthController adalah security risk. Endpoint ini mengekspos informasi internal sistem dan harus segera dihapus dari codebase.** |
| --- |

| // AuthController.java // HAPUS SELURUH METHOD INI: // @GetMapping("/debug") // public String tempDebug(...) { ... } // Untuk debugging gunakan logger: private static final Logger log = LoggerFactory.getLogger(AuthController.class); log.debug("Debug info: {}", value);  // aktif hanya di level DEBUG |
| --- |

## **1.6  [INFO] Entity dengan Business Logic — Rich Domain Model**

Class diagram secara eksplisit mendefinisikan method seperti konfirmasi(), batalkan(), tolak() di Appointment, dan cekTersedia(), tambahKuota(), kurangiKuota() di JadwalPraktik. Ini adalah desain Rich Domain Model yang VALID dan SESUAI class diagram — bukan pelanggaran.

| **Catatan Penting** Karena class diagram adalah acuan utama, method-method ini harus DIPERTAHANKAN di entity. Yang perlu dipastikan: method di entity hanya mengubah state internal entity, sedangkan interaksi dengan repository/database tetap dilakukan di service. |
| --- |

| // Appointment.java — PERTAHANKAN method ini (sesuai class diagram) public void konfirmasi() {     this.status = "DIKONFIRMASI";  // hanya ubah state internal } public void batalkan() {     this.status = "DIBATALKAN"; } public void tolak(String alasan) {     this.status = "DITOLAK";     this.alasanTolak = alasan; } // AppointmentService.java — service yang save ke database public void konfirmasiAppointment(String id) {     Appointment apt = appointmentRepository.findById(id).orElseThrow();     apt.konfirmasi();  // panggil method entity     appointmentRepository.save(apt);  // service yang save } |
| --- |

# **Bab 2 — Solusi Pelanggaran OOP**

## **2.1  [CRITICAL] AdminRSService — God Class (SRP Violation)**

| **Masalah** AdminRSService menangani terlalu banyak tanggung jawab: dokter management, poli management, jadwal, file upload, ID generation, statistik, dan utilitas. Ini adalah God Class yang sulit di-maintain. |
| --- |

### **Solusi: Pemecahan Service Sesuai Struktur yang Ada**

Cek service yang sudah ada terlebih dahulu di folder service/. Beberapa sudah ada: PoliService.java, JadwalPraktikService.java, SpesialisasiService.java. Manfaatkan yang sudah ada, pindahkan logic yang salah tempat.

| **Logic di AdminRSService** | **Pindah ke** | **Alasan** |
| --- | --- | --- |
| kelolaDataDokter() | tetap di AdminRSService | Sesuai class diagram AdminRS.kelolaDataDokter() |
| kelolaDataPoli() | PoliService (sudah ada) | PoliService sudah tersedia |
| kelolaJadwal() | JadwalPraktikService (sudah ada) | JadwalPraktikService sudah tersedia |
| saveUploadedFile() | buat FileStorageService baru | Cross-cutting concern |
| generateDokterId() dll | buat IdGeneratorService baru | Reusable across services |
| translateDayToIndonesian() | buat DateTimeUtil baru | Utility function |
| statistik (getTotalPasien, dll) | tetap di AdminRSService | Sesuai class diagram AdminRS |

### **File baru yang perlu dibuat:**

| src/main/java/rsis/ ├── service/ │   ├── FileStorageService.java   // baru — handle upload file foto dokter │   └── IdGeneratorService.java   // baru — generate semua ID entity │ └── util/     └── DateTimeUtil.java         // baru — translate hari, format tanggal |
| --- |

### **Contoh implementasi FileStorageService:**

| // FileStorageService.java @Service public class FileStorageService {     private final String uploadDir = "uploads/dokter/";     public String saveFile(MultipartFile file, String fileName) throws IOException {         Path uploadPath = Paths.get(uploadDir);         if (!Files.exists(uploadPath)) {             Files.createDirectories(uploadPath);         }         Path filePath = uploadPath.resolve(fileName);         Files.copy(file.getInputStream(), filePath,             StandardCopyOption.REPLACE_EXISTING);         return filePath.toString();     }     public void deleteFile(String filePath) throws IOException {         Files.deleteIfExists(Paths.get(filePath));     } } |
| --- |

### **Contoh implementasi IdGeneratorService:**

| // IdGeneratorService.java @Service public class IdGeneratorService {     public String generateDokterId(long count) {         return String.format("dkt-%03d", count + 1);     }     public String generateNomorStr(String idDokter) {         return "STR-" + idDokter.toUpperCase();     }     public String generateJadwalId(long count) {         return String.format("jdw-%03d", count + 1);     }     public String generatePoliId(long count) {         return String.format("pol-%03d", count + 1);     } } |
| --- |

### **Contoh implementasi DateTimeUtil:**

| // DateTimeUtil.java public class DateTimeUtil {     private static final Map<String, String> DAY_MAP = Map.of(         "MONDAY", "SENIN", "TUESDAY", "SELASA",         "WEDNESDAY", "RABU", "THURSDAY", "KAMIS",         "FRIDAY", "JUMAT", "SATURDAY", "SABTU",         "SUNDAY", "MINGGU"     );     public static String translateDayToIndonesian(String englishDay) {         return DAY_MAP.getOrDefault(englishDay.toUpperCase(), englishDay);     } } |
| --- |

## **2.2  [WARNING] Hard-Coded Status Strings (OCP Violation)**

| **Masalah** Status seperti "MENUNGGU", "DIKONFIRMASI", "SELESAI", "DIBATALKAN", "DITOLAK", "TERSEDIA", "PENUH", "LIBUR" di-hardcode di banyak file. Database sudah menggunakan CHECK constraint untuk nilai-nilai ini. |
| --- |

### **Solusi: Buat Enum yang Sesuai Database Schema**

| // AppointmentStatus.java (baru di package model atau constants) public enum AppointmentStatus {     MENUNGGU, DIKONFIRMASI, DITOLAK, DIBATALKAN, SELESAI;     // Nilai ini sesuai CHECK constraint di tabel appointment PostgreSQL } // JadwalStatus.java public enum JadwalStatus {     TERSEDIA, PENUH, LIBUR;     // Nilai ini sesuai CHECK constraint di tabel jadwal_praktik } // HariKerja.java public enum HariKerja {     SENIN, SELASA, RABU, KAMIS, JUMAT, SABTU, MINGGU;     // Nilai ini sesuai CHECK constraint di kolom hari } // Penggunaan di entity/service: apt.setStatus(AppointmentStatus.DIKONFIRMASI.name()); if (AppointmentStatus.MENUNGGU.name().equals(apt.getStatus())) { ... } |
| --- |

## **2.3  [WARNING] PasienController — Multiple Responsibilities (SRP)**

PasienController saat ini menangani: dashboard, profil, cari dokter, booking, riwayat, dan detail appointment. Sesuai struktur folder yang ada, sudah ada AppointmentController.java yang bisa dimanfaatkan.

| **Responsibility** | **Pindah ke Controller** | **Status** |
| --- | --- | --- |
| dashboard() | PasienController (tetap) | Sudah ada |
| profil management | PasienController (tetap) | Sudah ada |
| cariDokter() | PasienController (tetap) | Sudah ada |
| bookingAppointment() | AppointmentController (pindahkan) | Sudah ada, manfaatkan |
| jadwalRiwayat() | AppointmentController (pindahkan) | Sudah ada, manfaatkan |
| getAppointmentDetail() | AppointmentController (pindahkan) | Sudah ada, manfaatkan |

## **2.4  [INFO] Duplicate Transient Field Population (DRY Violation)**

Logic populate transient field Dokter dari tabel users ter-duplikasi di AdminRSService, DokterService, PasienService, dan AppointmentService.

### **Solusi: Centralize di DokterService (sudah ada)**

| // DokterService.java — satu method yang dipakai semua service lain public Dokter enrichWithUserData(Dokter dokter) {     userRepository.findById(dokter.getIdUser()).ifPresent(user -> {         dokter.setNama(user.getNama());         dokter.setEmail(user.getEmail());         dokter.setNomorHp(user.getNomorHp());     });     return dokter; } // Di service lain, inject DokterService dan panggil: @Autowired private DokterService dokterService; // Lalu ganti semua logika populate manual dengan: dokter = dokterService.enrichWithUserData(dokter); |
| --- |

# **Bab 3 — Solusi Logic Salah Tempat**

Berikut peta perpindahan yang konkret untuk setiap logic yang ditemukan di tempat yang salah, berdasarkan class diagram sebagai acuan.

| **Method / Logic** | **Lokasi Sekarang** | **Pindah ke** | **Dasar dari Class Diagram** |
| --- | --- | --- | --- |
| Auto-update status appointment | PasienController | AppointmentService | Appointment.konfirmasi/batalkan() |
| Populate transient Dokter | PasienController (x4 method) | DokterService | Dokter extends User |
| Statistik jadwal (count) | AdminController | AdminRSService | AdminRS.getTotalPasienHariIni() |
| safeDashboardValue() | AdminController | Hapus — pakai try/catch di service | - |
| getJadwalDokter() — build structure | DokterController | JadwalPraktikService | JadwalPraktik.getTanggal() |
| saveUploadedFile() | AdminRSService | FileStorageService (baru) | Cross-cutting concern |
| generateXxxId() semua | AdminRSService | IdGeneratorService (baru) | Reusable utility |
| translateDayToIndonesian() | AdminRSService | DateTimeUtil (baru) | Utility function |
| tempDebug() | AuthController | Hapus sepenuhnya | Security risk |
| bookingAppointment logic | PasienController | AppointmentService + AppointmentController | Pasien.bookingAppointment() |

# **Bab 4 — Rencana Implementasi Bertahap**

Lakukan refactoring secara bertahap agar tidak membreak fungsionalitas yang sudah berjalan. Pastikan aplikasi tetap bisa dijalankan setelah setiap fase.

## **Fase 1 — Quick Win (Prioritas Tertinggi, ~1-2 hari)**

| **Target Fase 1** Perbaikan yang paling krusial dan tidak memerlukan refactoring besar. Fokus pada security dan pelanggaran CRITICAL. |
| --- |

- **[CRITICAL]  **Hapus tempDebug() dari AuthController — 1 menit, hapus saja

- **[CRITICAL]  **Buat AppointmentStatus, JadwalStatus, HariKerja enum — hindari hard-coded strings

- **[CRITICAL]  **Tambahkan updateExpiredAppointments() di AppointmentService, panggil dari controller

- **[CRITICAL]  **Pindahkan logika populate transient Dokter ke DokterService.enrichWithUserData()

## **Fase 2 — Refactoring Service Layer (~2-3 hari)**

- **[CRITICAL]  **Buat FileStorageService — pindahkan saveUploadedFile() dari AdminRSService

- **[CRITICAL]  **Buat IdGeneratorService — pindahkan semua generateXxxId() dari AdminRSService

- **[WARNING]  **Buat DateTimeUtil — pindahkan translateDayToIndonesian()

- **[WARNING]  **Tambahkan getJadwalStatistics() di AdminRSService, hapus addJadwalStatsToModel() dari controller

## **Fase 3 — Refactoring Controller Layer (~2-3 hari)**

- **[WARNING]  **Pindahkan booking & riwayat appointment dari PasienController ke AppointmentController

- **[WARNING]  **Pindahkan getJadwalDokter() structure building dari DokterController ke JadwalPraktikService

- **[INFO]  **Hapus safeDashboardValue() dari AdminController — ganti dengan try/catch di service

## **Fase 4 — Finalisasi ****&**** Validasi (~1 hari)**

- Jalankan seluruh unit test yang ada (PasienServiceTest, DokterServiceTest, AppointmentServiceTest)

- Pastikan semua halaman Thymeleaf masih render dengan benar

- Test manual setiap flow utama: login, booking, konfirmasi, batalkan

- Update file audit.md dengan status 'RESOLVED' untuk setiap item yang sudah diperbaiki

# **Bab 5 — Struktur Folder Setelah Refactoring**

Berikut perubahan struktur folder yang diperlukan. Folder yang sudah ada tidak perlu diubah, hanya penambahan file baru.

| src/main/java/rsis/ ├── config/          (tidak berubah) ├── dto/             (tidak berubah) ├── model/ │   ├── interfaces/  (tidak berubah) │   ├── enums/       << BARU │   │   ├── AppointmentStatus.java │   │   ├── JadwalStatus.java │   │   └── HariKerja.java │   └── ... (entity lain tidak berubah) │ ├── repository/      (tidak berubah) │ ├── service/ │   ├── FileStorageService.java   << BARU │   ├── IdGeneratorService.java   << BARU │   └── ... (service lain refactor, tidak buat file baru) │ ├── controller/      (refactor isi, tidak buat file baru) │ └── util/            << BARU (folder)     └── DateTimeUtil.java         << BARU |
| --- |

# **Bab 6 — Checklist Verifikasi**

Gunakan checklist ini setelah menyelesaikan setiap fase refactoring untuk memastikan tidak ada yang terlewat.

## **Checklist MVC**

- Controller tidak memanggil repository secara langsung

- Controller tidak melakukan date comparison atau status update manual

- Semua business logic ada di service layer

- Method helper utility tidak ada di controller

- Debug endpoint tempDebug() sudah dihapus

- Thymeleaf template tidak mengandung business logic

## **Checklist OOP**

- AdminRSService tidak lagi menangani file upload (ada di FileStorageService)

- AdminRSService tidak lagi men-generate ID (ada di IdGeneratorService)

- AdminRSService tidak lagi translate hari (ada di DateTimeUtil)

- Enum AppointmentStatus, JadwalStatus, HariKerja sudah dibuat dan digunakan

- Logic populate transient Dokter hanya ada di DokterService

- Tidak ada string status yang di-hardcode (gunakan enum.name())

## **Checklist Kesesuaian Class Diagram**

- Appointment.konfirmasi(), batalkan(), tolak() masih ada di entity

- JadwalPraktik.cekTersedia(), tambahKuota(), kurangiKuota() masih ada di entity

- Pasien.bookingAppointment() diimplementasi via AppointmentService

- AdminRS.cetakLaporanBulanan() diimplementasi via LaporanBulananService

- INotifiable dan ISchedulable masih diimplementasi oleh class yang sesuai

- Relasi antar entity sesuai dengan skema database (foreign key)

# **Penutup**

Secara keseluruhan, codebase RSIS sudah memiliki fondasi yang baik — struktur folder sudah MVC, Thymeleaf template bersih, repository layer sudah benar, dan ada DTOs. Masalah yang ditemukan bersifat incremental dan dapat diperbaiki tanpa merombak ulang seluruh sistem.

| **Hal yang Sudah Baik (Tidak Perlu Diubah)** Struktur folder sudah mengikuti MVC dengan pemisahan yang jelas Repository layer sudah bersih tanpa business logic Thymeleaf templates sudah bersih tanpa logic yang embedded DTOs sudah digunakan dengan baik untuk data transfer Security configuration sudah proper dengan role-based access control INotifiable dan ISchedulable interface sudah mengikuti Interface Segregation Principle Entity dengan Rich Domain Model sesuai class diagram — PERTAHANKAN |
| --- |