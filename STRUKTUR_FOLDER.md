## Struktur Folder dan File

```bash
src/
├── main/
│   ├── java/rsis/
│   │   ├── config/
│   │   │   ├── AuthBeansConfig.java         → Konfigurasi bean autentikasi
│   │   │   ├── SecurityConfig.java          → Atur akses halaman per role, login, logout, enkripsi password
│   │   │   ├── ThymeleafConfig.java         → Konfigurasi tambahan Thymeleaf
│   │   │   └── WebConfig.java               → Konfigurasi web MVC
│   │   │
│   │   ├── dto/
│   │   │   ├── BookingRequestDTO.java       → DTO untuk request booking appointment
│   │   │   ├── JadwalDTO.java               → DTO untuk data jadwal praktik
│   │   │   ├── AppointmentResponseDTO.java  → DTO untuk response appointment
│   │   │   ├── BusiestDoctorDTO.java        → DTO untuk data dokter terbanyak pasien
│   │   │   ├── NotifikasiDTO.java           → DTO untuk data notifikasi
│   │   │   ├── ProfilUpdateDTO.java         → DTO untuk update profil
│   │   │   └── VisitStatistics.java         → DTO untuk statistik kunjungan
│   │   │
│   │   ├── model/
│   │   │   ├── interfaces/
│   │   │   │   ├── INotifiable.java         → Interface kontrak penerimaan notifikasi (untuk dokter dan pasien)
│   │   │   │   └── ISchedulable.java        → Interface kontrak pengelolaan jadwal (untuk dokter)
│   │   │   │
│   │   │   ├── User.java                    → Entity base semua pengguna (JOINED inheritance, implements INotifiable)
│   │   │   ├── Pasien.java                  → Entity pasien, extends User
│   │   │   ├── Dokter.java                  → Entity dokter (table dokter, extends User, implements ISchedulable, has id_dokter)
│   │   │   ├── AdminRS.java                 → Entity admin (table admin_rs, extends User, has id_admin)
│   │   │   ├── Poli.java                    → Entity poli/unit layanan rumah sakit
│   │   │   ├── Spesialisasi.java            → Entity spesialisasi dokter
│   │   │   ├── JadwalPraktik.java           → Entity jadwal praktik dokter
│   │   │   ├── Appointment.java             → Entity booking appointment pasien dengan dokter
│   │   │   ├── Notifikasi.java              → Entity notifikasi status appointment
│   │   │   └── LaporanBulanan.java          → Class untuk generate & export laporan PDF/CSV
│   │   │
│   │   ├── repository/
│   │   │   ├── UserRepository.java            → Akses tabel users untuk autentikasi (findByEmail)
│   │   │   ├── PasienRepository.java        → Akses tabel pasien (JOINED inheritance)
│   │   │   ├── DokterRepository.java        → Akses tabel dokter (JOINED inheritance)
│   │   │   ├── AdminRSRepository.java       → Akses tabel admin_rs (JOINED inheritance)
│   │   │   ├── PoliRepository.java          → Akses tabel poli
│   │   │   ├── SpesialisasiRepository.java  → Akses tabel spesialisasi
│   │   │   ├── JadwalPraktikRepository.java → Akses tabel jadwal_praktik
│   │   │   ├── AppointmentRepository.java   → Akses tabel appointment
│   │   │   └── NotifikasiRepository.java    → Akses tabel notifikasi
│   │   │
│   │   ├── service/
│   │   │   ├── AuthService.java             → Logika login, register, logout
│   │   │   ├── UserService.java             → Load user dari database untuk Spring Security
│   │   │   ├── PasienService.java           → Logika bisnis fitur pasien
│   │   │   ├── DokterService.java           → Logika bisnis fitur dokter
│   │   │   ├── AdminRSService.java          → Logika bisnis fitur admin
│   │   │   ├── PoliService.java             → Logika CRUD poli
│   │   │   ├── SpesialisasiService.java     → Logika CRUD spesialisasi
│   │   │   ├── JadwalPraktikService.java    → Logika kelola jadwal praktik
│   │   │   ├── AppointmentService.java      → Logika booking, batalkan, reschedule
│   │   │   ├── NotifikasiService.java       → Logika kirim & kelola notifikasi
│   │   │   └── LaporanBulananService.java   → Logika generate & export laporan PDF/CSV
│   │   │
│   │   ├── controller/
│   │   │   ├── AuthController.java          → Handle /login, /register, /logout
│   │   │   ├── PasienController.java        → Handle halaman & aksi fitur pasien
│   │   │   ├── DokterController.java        → Handle halaman & aksi fitur dokter
│   │   │   ├── AdminController.java         → Handle halaman & aksi fitur admin
│   │   │   ├── AppointmentController.java   → Handle booking, batalkan, reschedule
│   │   │   ├── NotifikasiController.java    → Handle notifikasi
│   │   │   ├── PoliController.java          → Handle CRUD poli
│   │   │   └── LaporanController.java       → Handle export laporan PDF/CSV
│   │   │
│   │   └── RsisApplication.java             → Entry point aplikasi Spring Boot
│   │
│   └── resources/
│       ├── templates/
│       │   ├── layout/
│       │   │   ├── base.html                → Template utama (navbar, footer, head)
│       │   │   └── navbar.html              → Navbar dinamis berdasarkan role
│       │   │
│       │   ├── fragments/
│       │   │   ├── auth/
│       │   │   │   ├── login-form.html       → Fragment form login
│       │   │   │   └── register-form.html    → Fragment form register
│       │   │   ├── overlay/
│       │   │   │   ├── admin/
│       │   │   │   │   ├── edit-dokter.html   → Overlay edit dokter
│       │   │   │   │   ├── edit-jadwal.html   → Overlay edit jadwal
│       │   │   │   │   ├── edit-poli.html     → Overlay edit poli
│       │   │   │   │   ├── hapus-dokter.html  → Overlay hapus dokter
│       │   │   │   │   ├── hapus-jadwal.html  → Overlay hapus jadwal
│       │   │   │   │   ├── hapus-poli.html    → Overlay hapus poli
│       │   │   │   │   ├── tambah-dokter.html → Overlay tambah dokter
│       │   │   │   │   ├── tambah-jadwal.html → Overlay tambah jadwal
│       │   │   │   │   └── tambah-poli.html   → Overlay tambah poli
│       │   │   │   ├── dokter/
│       │   │   │   │   ├── detail-appointment.html → Overlay detail appointment
│       │   │   │   │   └── tolak-appointment.html  → Overlay tolak appointment
│       │   │   │   ├── pasien/
│       │   │   │   │   ├── detail-riwayat-booking.html → Overlay detail riwayat
│       │   │   │   │   └── konfirmasi-booking.html     → Overlay konfirmasi booking
│       │   │   │   └── notifikasi.html       → Overlay notifikasi
│       │   │   ├── jadwal-dokter.html       → Fragment jadwal dokter
│       │   │   └── profil.html              → Fragment profil
│       │   │
│       │   ├── auth/
│       │   │   └── auth.html                 → Halaman autentikasi (login & register)
│       │   │
│       │   ├── pasien/
│       │   │   ├── dashboard.html           → Dashboard utama pasien
│       │   │   ├── cari-dokter.html         → Halaman pencarian dokter & poli
│       │   │   ├── booking.html             → Form booking appointment
│       │   │   ├── jadwal-riwayat.html      → Riwayat jadwal appointment
│       │   │   └── profil.html              → Halaman pengaturan profil
│       │   │
│       │   ├── dokter/
│       │   │   ├── dashboard.html           → Dashboard utama dokter
│       │   │   ├── jadwal-praktik.html      → Halaman kelola jadwal praktik
│       │   │   ├── appointment.html         → Halaman konfirmasi/tolak appointment
│       │   │   └── profil.html              → Halaman pengaturan profil
│       │   │
│       │   ├── admin/
│       │   │   ├── dashboard.html           → Dashboard statistik admin
│       │   │   ├── kelola-dokter.html       → Halaman CRUD data dokter
│       │   │   ├── kelola-poli.html         → Halaman CRUD data poli
│       │   │   ├── kelola-jadwal.html       → Halaman kelola semua jadwal
│       │   │   └── profil.html              → Halaman pengaturan profil
│       │   │
│       │   ├── landing.html                 → Halaman landing page
│       │   │
│       │   └── error/
│       │       ├── 403.html                 → Halaman akses ditolak
│       │       ├── 404.html                 → Halaman tidak ditemukan
│       │       └── 500.html                 → Halaman error server
│       │
│       ├── static/
│       │   ├── css/
│       │   │   ├── style.css                → Style global seluruh halaman
│       │   │   ├── admin/
│       │   │   │   ├── dashboard.css         → Style dashboard admin
│       │   │   │   ├── kelola-dokter.css    → Style kelola dokter
│       │   │   │   ├── kelola-jadwal.css    → Style kelola jadwal
│       │   │   │   └── kelola-poli.css      → Style kelola poli
│       │   │   ├── auth/
│       │   │   │   └── auth.css             → Style halaman login & register
│       │   │   ├── dokter/
│       │   │   │   ├── appointment.css      → Style appointment dokter
│       │   │   │   ├── dashboard.css        → Style dashboard dokter
│       │   │   │   └── jadwal-praktik.css   → Style jadwal praktik
│       │   │   ├── fragments/
│       │   │   │   ├── layout/
│       │   │   │   │   └── navbar.css       → Style navbar
│       │   │   │   ├── overlay/
│       │   │   │   │   ├── admin/           → Style overlay admin
│       │   │   │   │   ├── dokter/          → Style overlay dokter
│       │   │   │   │   ├── pasien/          → Style overlay pasien
│       │   │   │   │   └── notifikasi.css   → Style notifikasi
│       │   │   │   ├── jadwal-dokter.css    → Style jadwal dokter
│       │   │   │   └── profil.css           → Style profil
│       │   │   ├── landing/
│       │   │   │   └── landing.css          → Style halaman landing
│       │   │   └── pasien/
│       │   │       ├── booking.css          → Style booking
│       │   │       ├── cari-dokter.css     → Style pencarian dokter
│       │   │       ├── dashboard.css        → Style dashboard pasien
│       │   │       └── jadwal-riwayat.css   → Style riwayat jadwal
│       │   ├── js/
│       │   │   └── smooth-scroll.js         → Script smooth scroll
│       │   └── images/
│       │       ├── LandingPage/              → Gambar untuk landing page
│       │       ├── dokter.png               → Icon dokter
│       │       ├── logo-rsis.png             → Logo RSIS
│       │       ├── logo-rsis-v2.png          → Logo RSIS versi 2
│       │       └── logo-tulisan-rsis.png     → Logo tulisan RSIS
│       │
│       └── application.properties           → Konfigurasi database & aplikasi
│
└── test/
    └── java/rsis/
        ├── service/
        │   ├── PasienServiceTest.java        → Unit test service pasien
        │   ├── DokterServiceTest.java        → Unit test service dokter
        │   ├── AppointmentServiceTest.java   → Unit test service appointment
        │   ├── PrintDbDataTest.java         → Test print data database
        │   └── SyncJadwalQuotaTest.java     → Test sinkronisasi kuota jadwal
        ├── util/                             → Utility untuk testing
        └── RsisApplicationTests.java         → Test utama aplikasi Spring Boot

# File dan Folder Root
├── .dockerignore                          → File yang diabaikan Docker
├── .git/                                  → Repository Git
├── .gitattributes                         → Atribut Git
├── .gitignore                             → File yang diabaikan Git
├── .mvn/                                  → Maven wrapper
├── .agents/                               → Konfigurasi agen AI
├── .idea/                                 → Konfigurasi IntelliJ IDEA
├── .vscode/                               → Konfigurasi VS Code
├── ALUR_APLIKASI.md                       → Dokumentasi alur aplikasi
├── Class Diagram.svg                      → Diagram kelas aplikasi
├── Daftar_Tabel_dan_Data_Database_RSIS.sql → SQL tabel dan data database
├── Dockerfile                             → Konfigurasi Docker
├── HELP.md                                → Dokumentasi bantuan
├── PLAN.md                                → Rencana pengembangan
├── README.md                              → Dokumentasi project
├── STRUKTUR_FOLDER.md                     → Dokumentasi struktur folder
├── audit.md                               → Dokumentasi audit
├── data/                                  → Folder database H2
├── deploy.sh                              → Script deployment
├── migration_to_joined_table_inheritance.sql → SQL migrasi inheritance
├── mvnw                                   → Maven wrapper (Unix)
├── mvnw.cmd                               → Maven wrapper (Windows)
├── pom.xml                                → Konfigurasi Maven
├── run.log                                → Log aplikasi
├── skills-lock.json                       → Lock file skills
└── uploads/                               → Folder upload file
    └── dokter/                             → Upload foto dokter
```
