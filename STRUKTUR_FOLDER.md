## Struktur Folder dan File

```bash
src/
├── main/
│   ├── java/rsis/
│   │   ├── config/
│   │   │   ├── AuthBeansConfig.java         → Konfigurasi bean autentikasi
│   │   │   ├── SecurityConfig.java          → Atur akses halaman per role, login, logout, enkripsi password
│   │   │   └── ThymeleafConfig.java         → Konfigurasi tambahan Thymeleaf
│   │   │
│   │   ├── dto/
│   │   │   ├── BookingRequestDTO.java       → DTO untuk request booking appointment
│   │   │   ├── JadwalDTO.java               → DTO untuk data jadwal praktik
│   │   │   └── StatistikDTO.java            → DTO untuk data statistik dashboard
│   │   │
│   │   ├── model/
│   │   │   ├── interfaces/
│   │   │   │   ├── INotifiable.java         → Interface kontrak penerimaan notifikasi
│   │   │   │   └── ISchedulable.java        → Interface kontrak pengelolaan jadwal
│   │   │   │
│   │   │   ├── User.java                    → Entity base semua pengguna (JOINED inheritance, implements INotifiable)
│   │   │   ├── AppUser.java                 → Entity user aplikasi
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
│   │   │   ├── AppUserRepository.java       → Akses tabel app_user
│   │   │   ├── PasienRepository.java        → Akses tabel pasien (JOINED inheritance)
│   │   │   ├── DokterRepository.java        → Akses tabel dokter (JOINED inheritance)
│   │   │   ├── AdminRSRepository.java       → Akses tabel admin_rs (JOINED inheritance)
│   │   │   ├── PoliRepository.java          → Akses tabel poli di Supabase
│   │   │   ├── SpesialisasiRepository.java  → Akses tabel spesialisasi di Supabase
│   │   │   ├── JadwalPraktikRepository.java → Akses tabel jadwal_praktik di Supabase
│   │   │   ├── AppointmentRepository.java   → Akses tabel appointment di Supabase
│   │   │   └── NotifikasiRepository.java    → Akses tabel notifikasi di Supabase
│   │   │
│   │   ├── service/
│   │   │   ├── AuthService.java             → Logika login, register, logout
│   │   │   ├── UserService.java             → Load user dari database untuk Spring Security
│   │   │   ├── PasienService.java           → Logika bisnis fitur pasien
│   │   │   ├── DokterService.java           → Logika bisnis fitur dokter
│   │   │   ├── AdminRSService.java          → Logika bisnis fitur admin
│   │   │   ├── SpesialisasiService.java     → Logika CRUD spesialisasi
│   │   │   ├── JadwalPraktikService.java    → Logika kelola jadwal praktik
│   │   │   ├── AppointmentService.java      → Logika booking, batalkan, reschedule
│   │   │   ├── NotifikasiService.java       → Logika kirim & kelola notifikasi
│   │   │   ├── DashboardService.java        → Logika kalkulasi statistik dashboard admin
│   │   │   └── LaporanBulananService.java   → Logika generate & export laporan PDF/CSV
│   │   │
│   │   ├── controller/
│   │   │   ├── AuthController.java          → Handle /login, /register, /logout
│   │   │   ├── PasienController.java        → Handle halaman & aksi fitur pasien
│   │   │   ├── DokterController.java        → Handle halaman & aksi fitur dokter
│   │   │   ├── AdminController.java         → Handle halaman & aksi fitur admin
│   │   │   ├── AppointmentController.java   → Handle booking, batalkan, reschedule
│   │   │   ├── JadwalController.java        → Handle kelola jadwal praktik
│   │   │   └── LaporanController.java       → Handle export laporan PDF/CSV
│   │   │
│   │   └── RsisApplication.java             → Entry point aplikasi Spring Boot
│   │
│   └── resources/
│       ├── templates/
│       │   ├── layout/
│       │   │   ├── base.html                → Template utama (navbar, footer, head)
│       │   │   ├── navbar-pasien.html       → Navbar khusus role pasien
│       │   │   ├── navbar-dokter.html       → Navbar khusus role dokter
│       │   │   └── navbar-admin.html        → Navbar khusus role admin
│       │   │
│       │   ├── fragments/
│       │   │   ├── auth/
│       │   │   │   ├── login-form.html       → Fragment form login
│       │   │   │   └── register-form.html    → Fragment form register
│       │   │   ├── notifikasi.html           → Fragment notifikasi
│       │   │   └── sidebar-profil.html      → Fragment sidebar profil
│       │   │
│       │   ├── auth/
│       │   │   └── auth.html                 → Halaman autentikasi (login & register)
│       │   │
│       │   ├── pasien/
│       │   │   ├── dashboard.html           → Dashboard utama pasien
│       │   │   ├── cari-dokter.html         → Halaman pencarian dokter & poli
│       │   │   ├── jadwal-dokter.html       → Halaman lihat jadwal dokter
│       │   │   ├── booking.html             → Form booking appointment
│       │   │   ├── jadwal-riwayat.html      → Riwayat jadwal appointment
│       │   │   └── profil.html              → Halaman pengaturan profil
│       │   │
│       │   ├── dokter/
│       │   │   ├── dashboard.html           → Dashboard utama dokter
│       │   │   ├── jadwal.html              → Halaman kelola jadwal praktik
│       │   │   ├── daftar-pasien.html       → Daftar pasien yang booking
│       │   │   ├── appointment.html         → Halaman konfirmasi/tolak appointment
│       │   │   └── appointment-pending.html → Halaman appointment pending
│       │   │
│       │   ├── admin/
│       │   │   ├── dashboard.html           → Dashboard statistik admin
│       │   │   ├── kelola-dokter.html       → Halaman CRUD data dokter
│       │   │   ├── kelola-poli.html         → Halaman CRUD data poli
│       │   │   ├── kelola-jadwal.html       → Halaman kelola semua jadwal
│       │   │   ├── kelola-spesialisasi.html  → Halaman CRUD data spesialisasi
│       │   │   └── laporan-bulanan.html     → Halaman cetak & export laporan
│       │   │
│       │   ├── jadwal/
│       │   │   ├── list.html                → Halaman list jadwal
│       │   │   ├── create.html              → Halaman create jadwal
│       │   │   ├── edit.html                → Halaman edit jadwal
│       │   │   ├── dokter.html              → Halaman jadwal per dokter
│       │   │   └── available.html            → Halaman jadwal available
│       │   │
│       │   ├── laporan/
│       │   │   ├── index.html               → Halaman index laporan
│       │   │   └── bulanan.html             → Halaman laporan bulanan
│       │   │
│       │   ├── landing.html                 → Halaman landing page
│       │   │
│       │   └── error/
│       │       ├── 403.html                 → Halaman akses ditolak
│       │       └── 404.html                 → Halaman tidak ditemukan
│       │
│       ├── static/
│       │   ├── css/
│       │   │   ├── style.css                → Style global seluruh halaman
│       │   │   ├── auth/
│       │   │   │   └── auth.css              → Style khusus halaman login & register
│       │   │   ├── landing/
│       │   │   │   └── landing.css          → Style khusus halaman landing
│       │   │   ├── layout/
│       │   │   │   ├── navbar.css            → Style navbar
│       │   │   │   └── footer.css            → Style footer
│       │   │   ├── overlay/
│       │   │   │   └── overlay.css           → Style overlay
│       │   │   └── pasien/
│       │   │       ├── dashboard.css          → Style dashboard pasien
│       │   │       ├── cari-dokter.css       → Style pencarian dokter
│       │   │       ├── booking.css            → Style booking
│       │   │       ├── jadwal-dokter.css     → Style jadwal dokter
│       │   │       └── profil.css             → Style profil
│       │   ├── js/
│       │   │   ├── main.js                  → Script global seluruh halaman
│       │   │   ├── dashboard.js             → Script khusus halaman dashboard
│       │   │   └── smooth-scroll.js         → Script smooth scroll
│       │   └── images/
│       │       ├── LandingPage/              → Gambar untuk landing page
│       │       ├── logo-rsis.png             → Logo RSIS
│       │       ├── logo-rsis-v2.png          → Logo RSIS versi 2
│       │       └── logo-tulisan-rsis.png     → Logo tulisan RSIS
│       │
│       └── application.properties           → Konfigurasi database Supabase & aplikasi
│
└── test/
    └── java/rsis/
        ├── service/
        │   ├── PasienServiceTest.java        → Unit test service pasien
        │   ├── DokterServiceTest.java        → Unit test service dokter
        │   └── AppointmentServiceTest.java   → Unit test service appointment
        ├── util/                             → Utility untuk testing
        └── RsisApplicationTests.java         → Test utama aplikasi Spring Boot
```
