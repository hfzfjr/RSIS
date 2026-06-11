# RSIS - Rumah Sakit Information System

Sistem Informasi Rumah Sakit berbasis web yang dibangun dengan Spring Boot dan Thymeleaf. Aplikasi ini memudahkan pasien untuk mencari dokter, membuat appointment, dan mengelola jadwal konsultasi secara online.

## Prerequisites

Sebelum menjalankan project ini, pastikan Anda telah menginstal:

- **Java JDK 25** atau versi yang lebih baru
- **Maven** (untuk dependency management dan build)
- **PostgreSQL** (database)
- **Git** (untuk clone repository)

## Cara Menjalankan Project

### 1. Clone Repository

```bash
git clone <repository-url>
cd rsis
```

### 2. Konfigurasi Database

Buka file `src/main/resources/application.properties` dan sesuaikan konfigurasi database:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nama_database
spring.datasource.username=postgres
spring.datasource.password=password_anda
spring.datasource.driver-class-name=org.postgresql.Driver
```

### 3. Import Database Schema

Jalankan file SQL berikut untuk membuat tabel-tabel yang diperlukan:

- `Daftar_Tabel_dan_Data_Database_RSIS.sql` (jika tersedia)

Atau jalankan perintah SQL di PostgreSQL:

```sql
-- Buat database
CREATE DATABASE rsis;

-- Connect ke database
\c rsis

-- Jalankan schema SQL
```

### 4. Build Project

```bash
mvn clean install
```

### 5. Jalankan Aplikasi

```bash
mvn spring-boot:run
```

Atau jika menggunakan Maven wrapper:

```bash
./mvnw spring-boot:run
```

### 6. Akses Aplikasi

Buka browser dan akses:

- **Landing Page**: http://localhost:8080
- **Login/Register**: http://localhost:8080/auth
- **Dashboard Pasien**: http://localhost:8080/pasien/dashboard (setelah login)

## Fitur Utama

### Pasien
- Pencarian dokter berdasarkan spesialisasi
- Booking appointment dengan dokter
- Melihat jadwal praktik dokter
- Riwayat appointment
- Profil dan pengaturan akun

### Dokter
- Kelola jadwal praktik
- Lihat daftar pasien yang booking
- Konfirmasi/tolak appointment
- Dashboard statistik

### Admin
- CRUD data dokter, poli, spesialisasi
- Kelola jadwal praktik
- Generate laporan bulanan (PDF/CSV)
- Dashboard statistik

## Struktur Project

Untuk detail struktur folder dan file, lihat file [STRUKTUR_FOLDER.md](STRUKTUR_FOLDER.md)

## Default Credentials

Setelah menjalankan SQL schema, Anda dapat menggunakan akun default untuk testing:

- **Pasien**: Email sesuai yang terdaftar di database
- **Dokter**: Email sesuai yang terdaftar di database
- **Admin**: Email sesuai yang terdaftar di database

## Troubleshooting

### Port 8080 sudah digunakan
Jika port 8080 sudah digunakan, ubah port di `application.properties`:
```properties
server.port=8081
```

### Database connection error
Pastikan:
- PostgreSQL service sudah berjalan
- Database sudah dibuat
- Username dan password di `application.properties` sudah benar

### Maven build error
Jalankan:
```bash
mvn clean
mvn dependency:resolve
```

## License

Project ini dibuat untuk keperluan pembelajaran dan pengembangan sistem informasi rumah sakit.
