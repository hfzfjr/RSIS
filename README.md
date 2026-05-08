## Struktur Folder dan File

```bash
src/
├── main/
│   ├── java/rsis/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java          → Atur akses halaman per role, login, logout, enkripsi password
│   │   │   └── ThymeleafConfig.java         → Konfigurasi tambahan Thymeleaf
│   │   │
│   │   ├── model/
│   │   │   ├── interfaces/
│   │   │   │   ├── INotifiable.java         → Interface kontrak penerimaan notifikasi
│   │   │   │   └── ISchedulable.java        → Interface kontrak pengelolaan jadwal
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
│   │   │   ├── PoliService.java             → Logika CRUD poli
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
│       │   ├── auth/
│       │   │   ├── login.html               → Halaman login semua role
│       │   │   └── register.html            → Halaman register pasien baru
│       │   │
│       │   ├── pasien/
│       │   │   ├── dashboard.html           → Dashboard utama pasien
│       │   │   ├── cari-dokter.html         → Halaman pencarian dokter & poli
│       │   │   ├── jadwal-dokter.html       → Halaman lihat jadwal dokter
│       │   │   ├── booking.html             → Form booking appointment
│       │   │   └── riwayat-appointment.html → Riwayat & status appointment pasien
│       │   │
│       │   ├── dokter/
│       │   │   ├── dashboard.html           → Dashboard utama dokter
│       │   │   ├── jadwal.html              → Halaman kelola jadwal praktik
│       │   │   ├── daftar-pasien.html       → Daftar pasien yang booking
│       │   │   └── appointment.html         → Halaman konfirmasi/tolak appointment
│       │   │
│       │   ├── admin/
│       │   │   ├── dashboard.html           → Dashboard statistik admin
│       │   │   ├── kelola-dokter.html       → Halaman CRUD data dokter
│       │   │   ├── kelola-poli.html         → Halaman CRUD data poli
│       │   │   ├── kelola-jadwal.html       → Halaman kelola semua jadwal
│       │   │   └── laporan-bulanan.html     → Halaman cetak & export laporan
│       │   │
│       │   └── error/
│       │       ├── 403.html                 → Halaman akses ditolak
│       │       └── 404.html                 → Halaman tidak ditemukan
│       │
│       ├── static/
│       │   ├── css/
│       │   │   ├── style.css                → Style global seluruh halaman
│       │   │   ├── dashboard.css            → Style khusus halaman dashboard
│       │   │   └── auth.css                 → Style khusus halaman login & register
│       │   ├── js/
│       │   │   ├── main.js                  → Script global seluruh halaman
│       │   │   └── dashboard.js             → Script khusus halaman dashboard
│       │   └── images/
│       │       └── logo.png                 → Logo rumah sakit
│       │
│       └── application.properties           → Konfigurasi database Supabase & aplikasi
│
└── test/
    └── java/rsis/
        ├── service/
        │   ├── PasienServiceTest.java        → Unit test service pasien
        │   ├── DokterServiceTest.java        → Unit test service dokter
        │   └── AppointmentServiceTest.java   → Unit test service appointment
        └── RsisApplicationTests.java         → Test utama aplikasi Spring Boot
```
