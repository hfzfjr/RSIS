# PROJECT PLAN: RSIS (Rumah Sakit Intelligent System)

## 1. Deskripsi Proyek

**RSIS (Rumah Sakit Intelligent System)** adalah platform digital terintegrasi berbasis web yang dirancang untuk memanajemen jadwal dan booking dokter spesialis. Sistem ini dibangun untuk mengatasi antrean panjang dan ketidakefisienan pada proses pendaftaran manual, sekaligus memudahkan pasien, dokter, dan admin rumah sakit dalam mengelola layanan kesehatan secara real-time dan terstruktur.

Pembangunan sistem ini diimplementasikan menggunakan bahasa pemrograman **Java** dengan _framework_ **Spring Boot** untuk pengembangan _backend_ dan arsitektur kokoh berbasis _Object-Oriented Programming_ (OOP). Bagian antarmuka (_frontend_) menggunakan template engine **Thymeleaf** untuk menyajikan halaman web dinamis yang terintegrasi langsung dengan _controller_ Spring. Desain antarmuka dirancang dengan pendekatan minimalis yang mengutamakan struktur visual yang jelas. Mengacu pada desain terbaru, antarmuka menggunakan skema warna dominan putih dan hijau tua (_emerald/forest green_) dengan aksen yang senada. Pemilihan palet warna ini memberikan kesan segar, menenangkan, profesional, dan terpercaya khas instansi layanan kesehatan.

## 2. Tujuan & Sasaran

- Memfasilitasi proses _booking_ dan manajemen jadwal dokter spesialis secara real-time.
- Mengimplementasikan prinsip-prinsip _Object-Oriented Programming_ (OOP) murni di Java seperti _Class_, _Object_, _Inheritance_ (`extends`), _Encapsulation_ (akses _modifier_ privat dan _getter-setter_), serta _Interface_ (`implements`).
- Mempermudah pengelolaan operasional rumah sakit termasuk pelaporan, analitik, dan otomatisasi notifikasi.

## 3. Aktor & Target Pengguna

Sistem memiliki tiga aktor utama yang semuanya merupakan turunan dari kelas abstrak `User`:

1. **Pasien**: Pengguna yang mencari dokter, melihat jadwal ketersediaan, melakukan pemesanan (booking), serta mengelola (mengubah/membatalkan) jadwal appointment.
2. **Dokter**: Pengguna yang mengelola jadwal praktiknya sendiri, melihat daftar pasien yang terdaftar, serta melakukan konfirmasi atau penolakan appointment.
3. **Admin RS**: Pengelola sistem yang mengatur data master (dokter, poli), jadwal keseluruhan, serta memantau statistik operasional harian dan mencetak laporan bulanan.

## 4. Spesifikasi Sistem & Arsitektur (Berdasarkan Class Diagram)

Sistem dirancang memanfaatkan fitur-fitur OOP Java secara penuh. Berikut adalah spesifikasi teknis berdasarkan _Class Diagram_ utama:

### 4.1. Abstract Class & Interfaces

- **`public abstract class User`**: Kelas dasar (base class) bagi seluruh pengguna di sistem. Memiliki atribut ter-enkapsulasi: `idUser`, `nama`, `email`, `password`, `nomorHp`, dan `role`.
- **`public interface INotifiable`**: Mendefinisikan kontrak _method_ `terimaNotifikasi()` dan `getEmail()`. Kelas abstrak `User` mengimplementasikan _interface_ ini sehingga seluruh _subclass_ (Pasien, Dokter, AdminRS) secara otomatis memiliki kemampuan menerima notifikasi.
- **`public interface ISchedulable`**: Mendefinisikan kontrak manajemen jadwal (`getJadwal()`, `updateJadwal()`, `cekKetersediaan()`). Diimplementasikan secara khusus oleh kelas `Dokter`.

### 4.2. Core Classes & Entitas (Spring Boot Entity / Component)

_Catatan: Fungsionalitas statistik dilebur langsung ke dalam wewenang AdminRS sesuai dokumentasi pemodelan terbaru._

1. **Pasien**
   - Mewarisi: `User`
   - Atribut Khusus: `nomorRekamMedis`, `tanggalLahir`, `alamat`
   - Fungsionalitas: `cariDokter()`, `lihatJadwalDokter()`, `bookingAppointment()`, `batalkanAppointment()`, `rescheduleAppointment()`.
2. **Dokter**
   - Mewarisi: `User`, Mengimplementasi: `ISchedulable`
   - Atribut Khusus: `nomorStr`, `spesialisasi`, `poli`
   - Fungsionalitas: `kelolaJadwal()`, `lihatDaftarPasien()`, `konfirmasiAppointment()`, `tolakAppointment()`.
3. **AdminRS**
   - Mewarisi: `User`
   - Atribut Khusus: `jabatan`
   - Fungsionalitas: Kelola master data (`kelolaDataDokter()`, `kelolaDataPoli()`, `kelolaJadwal()`), Pelaporan (`cetakLaporanBulanan()`), dan **Statistik Langsung** (`getTotalPasienHariIni()`, `getTotalPasienBulanIni()`, `getDokterTersibuk()`, `getPasienPerHari()`).
4. **Poli & Spesialisasi**
   - **Poli**: Menyimpan data ruangan poli dan memiliki hubungan agregasi/asosiasi dengan daftar Dokter serta JadwalPraktik.
   - **Spesialisasi**: Mengkategorikan keahlian/bidang kedokteran.
5. **JadwalPraktik**
   - Berisi data waktu milik Dokter, meliputi atribut `hari`, `tanggal`, `jamMulai`, `jamSelesai`, `kuota`, dan `sisaKuota`. Memiliki method operasional `updateStatus()` dan `kurangiKuota()`.
6. **Appointment (Transaksi Inti)**
   - Menghubungkan entitas Pasien, Dokter, dan JadwalPraktik.
   - Memiliki status (_pending, confirmed, canceled_), nomor antrean otomatis, dan alasan penolakan jika dibatalkan oleh dokter.
7. **Notifikasi**
   - Objek pesan yang dikirimkan ke objek bertipe data referensi `INotifiable`.
8. **LaporanBulanan**
   - Digenerate oleh AdminRS, berisi rekapitulasi data appointment, total pasien, dan pembatalan, yang siap diekspor ke format eksternal (PDF/CSV).

### 4.3. Struktur Folder Proyek (Project Directory Structure)

Berikut adalah struktur folder proyek RSIS yang mengikuti konvensi standar Spring Boot:

```
rsis/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── rsis/
│   │   │           ├── RsisApplication.java              # Main class Spring Boot
│   │   │           ├── config/
│   │   │           │   ├── SecurityConfig.java           # Konfigurasi Spring Security
│   │   │           │   └── ThymeleafConfig.java          # Konfigurasi Thymeleaf
│   │   │           ├── model/
│   │   │           │   ├── User.java                     # Abstract class User
│   │   │           │   ├── Pasien.java                  # Entity Pasien (extends User)
│   │   │           │   ├── Dokter.java                  # Entity Dokter (extends User, implements ISchedulable)
│   │   │           │   ├── AdminRS.java                  # Entity AdminRS (extends User)
│   │   │           │   ├── Poli.java                     # Entity Poli
│   │   │           │   ├── Spesialisasi.java             # Entity Spesialisasi
│   │   │           │   ├── JadwalPraktik.java           # Entity JadwalPraktik
│   │   │           │   ├── Appointment.java             # Entity Appointment
│   │   │           │   ├── Notifikasi.java               # Entity Notifikasi
│   │   │           │   └── LaporanBulanan.java           # Entity LaporanBulanan
│   │   │           ├── interface/
│   │   │           │   ├── INotifiable.java              # Interface untuk notifikasi
│   │   │           │   └── ISchedulable.java             # Interface untuk manajemen jadwal
│   │   │           ├── repository/
│   │   │           │   ├── UserRepository.java           # Repository User
│   │   │           │   ├── PasienRepository.java        # Repository Pasien
│   │   │           │   ├── DokterRepository.java        # Repository Dokter
│   │   │           │   ├── AdminRSRepository.java        # Repository AdminRS
│   │   │           │   ├── PoliRepository.java           # Repository Poli
│   │   │           │   ├── SpesialisasiRepository.java   # Repository Spesialisasi
│   │   │           │   ├── JadwalPraktikRepository.java # Repository JadwalPraktik
│   │   │           │   ├── AppointmentRepository.java    # Repository Appointment
│   │   │           │   └── NotifikasiRepository.java      # Repository Notifikasi
│   │   │           ├── service/
│   │   │           │   ├── UserService.java             # Service User
│   │   │           │   ├── PasienService.java          # Service Pasien
│   │   │           │   ├── DokterService.java          # Service Dokter
│   │   │           │   ├── AdminRSService.java          # Service AdminRS
│   │   │           │   ├── JadwalPraktikService.java   # Service JadwalPraktik
│   │   │           │   ├── AppointmentService.java     # Service Appointment
│   │   │           │   ├── NotifikasiService.java       # Service Notifikasi
│   │   │           │   └── LaporanService.java          # Service Laporan
│   │   │           ├── controller/
│   │   │           │   ├── AuthController.java           # Controller untuk Login/Register
│   │   │           │   ├── PasienController.java        # Controller Dashboard Pasien
│   │   │           │   ├── DokterController.java        # Controller Dashboard Dokter
│   │   │           │   ├── AdminController.java         # Controller Dashboard Admin
│   │   │           │   ├── JadwalController.java        # Controller Manajemen Jadwal
│   │   │           │   ├── AppointmentController.java    # Controller Booking Appointment
│   │   │           │   └── LaporanController.java       # Controller Laporan
│   │   │           └── dto/
│   │   │               ├── BookingRequestDTO.java        # DTO untuk request booking
│   │   │               ├── JadwalDTO.java                # DTO untuk data jadwal
│   │   │               └── StatistikDTO.java            # DTO untuk data statistik
│   │   ├── resources/
│   │   │   ├── templates/
│   │   │   │   ├── index.html                           # Landing page
│   │   │   │   ├── login.html                           # Halaman login
│   │   │   │   ├── register.html                        # Halaman registrasi pasien
│   │   │   │   ├── pasien/
│   │   │   │   │   ├── dashboard.html                   # Dashboard Pasien
│   │   │   │   │   ├── cari-dokter.html                 # Halaman pencarian dokter
│   │   │   │   │   ├── jadwal-dokter.html               # Halaman jadwal dokter
│   │   │   │   │   ├── riwayat-appointment.html         # Halaman riwayat appointment
│   │   │   │   │   └── profil.html                      # Halaman profil pasien
│   │   │   │   ├── dokter/
│   │   │   │   │   ├── dashboard.html                   # Dashboard Dokter
│   │   │   │   │   ├── daftar-pasien.html               # Halaman daftar pasien
│   │   │   │   │   ├── kelola-jadwal.html               # Halaman kelola jadwal
│   │   │   │   │   └── notifikasi.html                  # Halaman notifikasi dokter
│   │   │   │   ├── admin/
│   │   │   │   │   ├── dashboard.html                   # Dashboard Admin
│   │   │   │   │   ├── kelola-dokter.html               # Halaman kelola data dokter
│   │   │   │   │   ├── kelola-poli.html                 # Halaman kelola data poli
│   │   │   │   │   ├── kelola-jadwal.html               # Halaman kelola jadwal
│   │   │   │   │   ├── statistik.html                   # Halaman statistik
│   │   │   │   │   └── laporan.html                     # Halaman laporan bulanan
│   │   │   │   ├── fragments/
│   │   │   │   │   ├── header.html                      # Fragment header
│   │   │   │   │   ├── footer.html                      # Fragment footer
│   │   │   │   │   └── navbar.html                      # Fragment navbar
│   │   │   │   └── error/
│   │   │   │       └── 404.html                         # Halaman error 404
│   │   │   ├── static/
│   │   │   │   ├── css/
│   │   │   │   │   └── style.css                         # File CSS utama
│   │   │   │   ├── js/
│   │   │   │   │   └── main.js                          # File JavaScript utama
│   │   │   │   └── images/
│   │   │   │       └── logo.png                         # Logo aplikasi
│   │   │   ├── application.properties                  # Konfigurasi aplikasi
│   │   │   └── application-dev.properties               # Konfigurasi environment development
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── rsis/
│   │                   ├── service/
│   │                   │   ├── PasienServiceTest.java   # Unit test PasienService
│   │                   │   ├── DokterServiceTest.java   # Unit test DokterService
│   │                   │   └── AppointmentServiceTest.java # Unit test AppointmentService
│   │                   └── controller/
│   │                       ├── PasienControllerTest.java # Integration test PasienController
│   │                       └── DokterControllerTest.java # Integration test DokterController
├── pom.xml                                              # Maven dependencies
└── README.md                                            # Dokumentasi proyek
```

**Penjelasan Struktur Folder:**

- **`config/`**: Berisi konfigurasi Spring Security dan Thymeleaf
- **`model/`**: Berisi semua entity class yang mengimplementasikan konsep OOP (inheritance, encapsulation, interface)
- **`interface/`**: Berisi interface INotifiable dan ISchedulable sesuai desain OOP
- **`repository/`**: Spring Data JPA repositories untuk akses database
- **`service/`**: Business logic layer yang mengimplementasikan fungsionalitas dari setiap entity
- **`controller/`**: Spring MVC controllers untuk menangani HTTP request dan render Thymeleaf templates
- **`dto/`**: Data Transfer Objects untuk transfer data antara layers
- **`templates/`**: Thymeleaf HTML templates untuk frontend, diorganisasi berdasarkan role (pasien, dokter, admin)
- **`fragments/`**: Reusable Thymeleaf fragments (header, footer, navbar)
- **`static/`**: Static assets (CSS, JS, images)
- **`test/`**: Unit dan integration tests

### 4.4. Teknologi & Database

**Teknologi yang Digunakan:**

- **Backend Framework**: Spring Boot 3.2.x
- **Java Version**: JDK 17 atau 21
- **Build Tool**: Maven
- **Frontend Template Engine**: Thymeleaf 3.1.x
- **Security**: Spring Security 6.x
- **ORM**: Spring Data JPA (Hibernate)
- **Database**: MySQL 8.0+ atau PostgreSQL 15+
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **PDF Generation**: Apache PDFBox atau iText (untuk laporan)
- **CSV Export**: Apache Commons CSV

**Konfigurasi Database:**

- **Connection Pool**: HikariCP (default Spring Boot)
- **Migration**: Flyway atau Liquibase (opsional, untuk version control schema)
- **Schema**: Database relasional dengan tabel-tabel yang sesuai dengan entity classes

**Dependencies Utama (pom.xml):**

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Database Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- PDF Generation -->
    <dependency>
        <groupId>com.itextpdf</groupId>
        <artifactId>itextpdf</artifactId>
        <version>8.0.2</version>
    </dependency>

    <!-- CSV Export -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-csv</artifactId>
        <version>1.10.0</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## 5. Alur Kerja Sistem (System Flow)

### 5.1. Alur Autentikasi dan Registrasi (General Flow)

- **Akses Halaman Utama:** Pengguna membuka sistem RSIS melalui _browser_. Antarmuka Thymeleaf menampilkan _landing page_ dengan nuansa warna hijau tua dan putih yang bersih.
- **Registrasi (Khusus Pasien):** Jika pengguna belum memiliki akun, mereka masuk ke halaman pendaftaran. Pada tahap ini, untuk mempercepat proses _onboarding_, **pengguna hanya perlu memasukkan Nama Lengkap, Email, dan Password saja.**
- **Login (Spring Security):** Pengguna memasukkan Email dan Password. Sistem memvalidasi kredensial dan memeriksa atribut `Role`.
- **Routing Dashboard:** Berdasarkan `Role`, pengguna akan diarahkan (_redirect_) ke:
  - Dashboard Pasien
  - Dashboard Dokter
  - Dashboard Admin RS

### 5.2. Alur Utama Pasien (Pencarian & Booking Appointment)

- **Pencarian Dokter:** Pada dashboard, Pasien memanggil fungsi `cariDokter()` dengan memasukkan kata kunci berupa _Spesialisasi_ atau Nama Dokter.
- **Melihat Jadwal:** Sistem menampilkan daftar Dokter beserta _card_ Poli-nya. Pasien menekan tombol "Lihat Jadwal" yang akan mengeksekusi `lihatJadwalDokter(dokterId)`.
- **Validasi Jadwal:** Sistem hanya akan menampilkan daftar `JadwalPraktik` yang `sisaKuota > 0` dan memiliki `statusKetersediaan` aktif/tersedia.
- **Proses Booking:**
  - Pasien memilih jam/slot jadwal dan mengklik "Booking".
  - Sistem akan mengecek kelengkapan data pribadi Pasien (seperti Nomor Rekam Medis, Tanggal Lahir, dan Alamat).
  - Jika informasi pribadi yang wajib diisi tersebut masih kosong, sistem akan menampilkan _pop-up_ yang berisi pesan peringatan kelengkapan data, beserta tombol "Batalkan" dan tombol "Lanjutkan".
  - Jika tombol "Lanjutkan" diklik, sistem akan langsung me-_redirect_ Pasien ke _page_ Profil untuk melengkapi data.
  - Jika data sudah lengkap (atau setelah Pasien melengkapi data dan mengulang klik booking), fungsi `bookingAppointment(jadwalId)` dieksekusi.
  - Sistem membuat satu _record_/objek `Appointment` baru dengan status default **"Pending"** dan men- _generate_ nomor antrean otomatis.
  - Sistem otomatis memanggil `kurangiKuota()` pada objek `JadwalPraktik` yang dipilih, sehingga sisa kuota berkurang secara _real-time_.
- **Notifikasi Awal:** Sistem memicu pengiriman `Notifikasi` (melalui _interface_ `INotifiable`) ke Email/Dashboard Dokter yang bersangkutan bahwa ada pesanan baru, serta notifikasi ke Pasien bahwa booking sedang diproses.

### 5.3. Alur Modifikasi Jadwal oleh Pasien (Reschedule & Batal)

- **Melihat Riwayat:** Pasien membuka menu "Jadwal & Riwayat" untuk melihat daftar konsultasi.
- **Skenario Batal:**
  - Jika Pasien memilih "Batalkan", fungsi `batalkanAppointment()` dipanggil.
  - Status `Appointment` berubah menjadi **"Canceled"**.
  - Sistem mengembalikan kuota jadwal (`sisaKuota` bertambah 1).
- **Skenario Reschedule:**
  - Jika Pasien memilih "Ubah Jadwal", fungsi `rescheduleAppointment()` dipanggil.
  - Pasien memilih slot `JadwalPraktik` yang baru.
  - Sistem membatalkan jadwal lama (kuota lama +1) dan mengaitkan objek `Appointment` tersebut ke `jadwalId` yang baru (kuota baru -1), lalu status kembali menjadi **"Pending"**.

### 5.4. Alur Utama Dokter (Manajemen Jadwal & Konfirmasi)

- **Melihat Antrean Pasien:** Dokter membuka menu "Daftar Appointment" di dashboard. Sistem menjalankan `lihatDaftarPasien()` untuk menampilkan semua `Appointment` yang terhubung ke dokter tersebut.
- **Skenario Konfirmasi:**
  - Dokter meninjau _request_ berstatus "Pending".
  - Jika disetujui, Dokter menekan tombol "Konfirmasi" -> memanggil `konfirmasiAppointment()`.
  - Status berubah menjadi **"Confirmed"**. Notifikasi persetujuan dikirim ke Pasien.
- **Skenario Penolakan:**
  - Jika jadwal tidak memungkinkan, Dokter menekan tombol "Tolak" -> memanggil `tolakAppointment()`.
  - Sistem akan menampilkan _modal box_ (UI Thymeleaf) yang mewajibkan Dokter mengisi "Alasan Penolakan" (misal: "Sedang ada operasi mendadak").
  - Status berubah menjadi **"Canceled"**, alasan penolakan disimpan di objek `Appointment`, dan kuota `JadwalPraktik` dikembalikan. Notifikasi penolakan + alasan dikirim ke Pasien.
- **Kelola Jadwal Praktik:** Melalui _interface_ `ISchedulable`, Dokter bisa memanggil `updateJadwal()` untuk memperbarui jam kerja, menambah slot, atau mengubah status ketersediaan (misal dari "Tersedia" menjadi "Libur/Penuh").

### 5.5. Alur Admin Rumah Sakit (Master Data, Statistik & Laporan)

- **Pemantauan Dashboard:** Saat login, Admin langsung melihat halaman ringkasan data. _Controller_ memanggil method pada objek `AdminRS` seperti `getTotalPasienHariIni()` dan `getDokterTersibuk()`, lalu menampilkannya dalam bentuk visual grafis sederhana atau angka sorotan (_highlights_).
- **Manajemen Data Master (CRUD):**
  - Admin dapat menambah, mengedit, atau menonaktifkan akun `Dokter`.
  - Admin dapat mengelola data `Poli` dan mengatur pemetaan antara Poli, Dokter, dan `Spesialisasi`.
  - Admin memiliki wewenang (_override_) untuk mengedit `JadwalPraktik` jika Dokter berhalangan hadir dan tidak sempat mengubah sistem sendiri.
- **Cetak Laporan Bulanan:**
  - Pada akhir bulan, Admin membuka menu "Laporan".
  - Admin memasukkan parameter _Bulan_ dan _Tahun_, lalu menekan tombol "Generate".
  - Fungsi `cetakLaporanBulanan()` pada `AdminRS` dijalankan untuk mengumpulkan total `Appointment` sukses, batal, dan total pasien.
  - Sistem memungkinkan Admin untuk men- _download_ rekapitulasi tersebut melalui `exportPDF()` atau `exportCSV()`.

## 6. Rencana Timeline & Pembagian Tugas Tim

Pengembangan proyek ini dibagi secara terstruktur hingga minggu ke-16 dengan pembagian penanggung jawab utama (PIC) sebagai berikut:

- **Desain Sistem & UML (Minggu 6):** Muhammad Rafiq Abdurrasyid
- **Perancangan Class/Arsitektur OOP Java (Minggu 7):** Muh Ishaq Afif Ismail
- **Implementasi Fitur Core, Spring Boot & Thymeleaf (Minggu 8-13):** Semua Anggota
- **Testing, Debugging & Validasi Sistem (Minggu 14):** Hafiz Fajar Ramadhan
- **Penyusunan Dokumentasi & Laporan (Minggu 15):** Muhammad Jordan Devna Nouvaleent
- **Finalisasi & Pengumpulan (Minggu 16):** Semua Anggota
