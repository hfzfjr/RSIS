# RSIS - Rumah Sakit Intelligent System

Sistem Informasi Rumah Sakit berbasis web yang dibangun dengan Spring Boot dan Thymeleaf. Aplikasi ini memudahkan pasien untuk mencari dokter, membuat appointment, dan mengelola jadwal konsultasi secara online.

## Prerequisites

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

Copy file `.env.example` ke `.env` dan sesuaikan konfigurasi database:

```bash
cp .env.example .env
```

Buka file `.env` dan sesuaikan konfigurasi:

```env
DB_URL=jdbc:postgresql://your-host:port/database?sslmode=require
DB_USERNAME=your-username
DB_PASSWORD=your-password
```

### 3. Build Project

```bash
mvn clean install
```

### 4. Jalankan Aplikasi

```bash
mvn spring-boot:run
```

### 5. Akses Aplikasi

Buka browser dan akses: http://localhost:8080

## Credential Per Role

| Role | Email | Password |
|------|-------|----------|
| Pasien | jokosusilo@gmail.com | pasien123 |
| Dokter | ahmad@gmail.com | dokter123 |
| Admin RS | budi@gmail.com | admin123 |

## Struktur Project

```
rsis/
├── src/main/java/rsis/
│   ├── controller/     # HTTP request handlers
│   ├── service/        # Business logic layer
│   ├── repository/     # Data access layer (Spring Data JPA)
│   ├── model/          # Entity models
│   ├── dto/            # Data Transfer Objects
│   └── config/         # Spring configuration
├── src/main/resources/
│   ├── templates/      # Thymeleaf HTML templates
│   ├── static/         # CSS, JS, images
│   └── application.properties
└── pom.xml             # Maven dependencies
```

## Fitur Per Role

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

### Admin RS
- CRUD data dokter, poli, spesialisasi
- Kelola jadwal praktik
- Generate laporan bulanan (PDF/CSV)
- Dashboard statistik
