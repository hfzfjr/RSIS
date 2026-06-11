# Dokumentasi Alur Aplikasi RSIS (Rumah Sakit Information System)

> **Acuan utama:** 
![alt text](<Class Diagram.svg>)
---

## 1. Alur Startup Aplikasi

### 1.1 Application Entry Point
**File:** `src/main/java/rsis/RsisApplication.java`

```java
package rsis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RsisApplication {
    public static void main(String[] args) {
        SpringApplication.run(RsisApplication.class, args);
    }
}
```

**Alur:**
1. JVM menjalankan `main()` method di `RsisApplication`
2. Spring Boot menginisialisasi application context
3. Spring Boot melakukan component scanning untuk menemukan semua `@Component`, `@Service`, `@Controller`, `@Repository`
4. Spring Boot menginisialisasi database connection (berdasarkan `application.properties`)
5. Spring Boot menginisialisasi Security filter chain
6. Spring Boot memulai embedded web server (Tomcat)
7. Aplikasi siap menerima HTTP requests

### 1.2 Konfigurasi Aplikasi

#### SecurityConfig
**File:** `src/main/java/rsis/config/SecurityConfig.java`

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import rsis.service.UserService;
```

**Fungsi:**
- Mengkonfigurasi Spring Security untuk autentikasi dan autorisasi
- Mendefinisikan URL yang diizinkan tanpa login (public endpoints)
- Mendefinisikan role-based access control (ADMIN, DOKTER, PASIEN)
- Mengkonfigurasi password encoder dengan BCrypt
- Mengkonfigurasi authentication success handler untuk redirect berdasarkan role

#### ThymeleafConfig
**File:** `src/main/java/rsis/config/ThymeleafConfig.java`

**Fungsi:**
- Mengkonfigurasi Thymeleaf template engine
- Mengatur prefix dan suffix untuk template files
- Menambahkan Spring Security dialect
- Menonaktifkan template caching untuk development

#### AuthBeansConfig
**File:** `src/main/java/rsis/config/AuthBeansConfig.java`

**Fungsi:**
- Mengkonfigurasi AuthenticationManager bean untuk autentikasi manual

---

## 2. Alur Autentikasi

### 2.1 Alur Registrasi Pasien Baru

**Endpoint:** `POST /register`

**Controller:** `src/main/java/rsis/controller/AuthController.java`

**Alur Lengkap:**
1. User mengakses halaman `/auth?tab=register`
2. `AuthController.authPage()` menampilkan form registrasi
3. User mengisi form (nama lengkap, email, password min 8 karakter, konfirmasi password)
4. User submit form ke `POST /register`
5. `AuthController.registerSubmit()` melakukan validasi:
   - Validasi field tidak kosong
   - Validasi password match
   - Validasi panjang password >= 8
6. Jika validasi gagal, kembali ke form dengan error message
7. Jika validasi sukses, panggil `AuthService.registerPasien()`

**Service:** `src/main/java/rsis/service/AuthService.java`

**Alur AuthService.registerPasien():**
1. Cek apakah email sudah terdaftar via `AppUserRepository.existsByEmailIgnoreCase()`
2. Generate user ID dengan format `usr-XXX` via `AppUserRepository.findLatestUserId()`
3. Generate pasien ID dengan format `psn-XXX` via `PasienRepository.findLatestPasienId()`
4. Buat `AppUser` entity: set idUser, nama, email, password (BCrypt encoded), role="PASIEN", createdAt
5. Simpan `AppUser` via `AppUserRepository.save()`
6. Buat `Pasien` entity: set idPasien, idUser (FK ke users), nama, email, password, role
7. Simpan `Pasien` via `PasienRepository.save()`
8. Autentikasi user secara otomatis via `AuthenticationManager.authenticate()`
9. Set authentication di `SecurityContextHolder` dan HTTP session
10. Redirect ke `/pasien/dashboard`

**Repository yang digunakan:**
- `AppUserRepository.existsByEmailIgnoreCase()` — cek email duplikat
- `AppUserRepository.findLatestUserId()` — generate user ID
- `AppUserRepository.save()` — simpan user baru
- `PasienRepository.findLatestPasienId()` — generate pasien ID
- `PasienRepository.save()` — simpan pasien baru

> **Perubahan:** `UserRepository` dihapus dan query-nya digabungkan ke dalam `AppUserRepository` untuk menghilangkan duplikasi repository yang memiliki fungsi sama.

---

### 2.2 Alur Login

**Endpoint:** `POST /login`

**Controller:** `src/main/java/rsis/controller/AuthController.java`

**Alur:**
1. User mengakses halaman `/auth?tab=login`
2. `AuthController.authPage()` menampilkan form login
3. User mengisi email dan password
4. User submit form ke `POST /login` (dikonfigurasi di SecurityConfig)
5. Spring Security menangani proses login:
   - `UserService.loadUserByUsername()` memuat user dari `AppUserRepository`
   - Password di-compare dengan encoded password via BCrypt
6. Jika login berhasil, redirect berdasarkan role:
   - ADMIN → `/admin/dashboard`
   - DOKTER → `/dokter/dashboard`
   - PASIEN → `/pasien/dashboard`
7. Jika login gagal: redirect ke `/auth?error`

**Service:** `src/main/java/rsis/service/UserService.java`

**Alur UserService.loadUserByUsername():**
1. Panggil `AppUserRepository.findByEmailIgnoreCase(email)`
2. Jika user tidak ditemukan, throw `UsernameNotFoundException`
3. Buat `SimpleGrantedAuthority` dengan role user
4. Return `UserDetails` object

**Repository yang digunakan:**
- `AppUserRepository.findByEmailIgnoreCase()` — cari user berdasarkan email

---

### 2.3 Alur Logout

**Endpoint:** `POST /logout`

**Alur:**
1. User mengklik tombol logout pada halaman `/pasien/profil`
2. Spring Security menangani logout: invalidate session, clear security context
3. Redirect ke `/auth?logout`

---

## 3. Alur Fitur Pasien

### 3.1 Alur Dashboard Pasien

**Endpoint:** `GET /pasien/dashboard`

**Controller:** `src/main/java/rsis/controller/PasienController.java`

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Appointment;
import rsis.model.AppUser;
import rsis.model.Dokter;
import rsis.model.Pasien;
import rsis.repository.AppUserRepository;
import rsis.repository.PasienRepository;
import rsis.service.AppointmentService;
import rsis.service.NotifikasiService;
import rsis.service.PasienService;
import java.util.List;
```

**Alur:**
1. User dengan role PASIEN mengakses `/pasien/dashboard`
2. Spring Security memverifikasi role user
3. `PasienController.dashboard()` dipanggil
4. Get `AppUser` via `AppUserRepository.findByEmailIgnoreCase(principal.getUsername())`
5. Get `Pasien` via `PasienRepository.findByEmail(appUser.getEmail())`
6. Get appointments via `AppointmentService.getAppointmentsByPasienId(pasienId)`
7. Get dokters via `PasienService.cariDokter(null)`
8. Get notifikasi via `NotifikasiService.getNotifikasiByPenerimaId(pasienId)`
9. Hitung statistik (total appointment, total dokter, total notifikasi)
10. Ambil 4 available doctors dan 2 upcoming appointments untuk display
11. Add semua data ke Model
12. Render template `pasien/dashboard.html`

**Aturan data dinamis:** Semua data (nama pasien, daftar dokter, riwayat appointment, notifikasi) harus berasal dari database melalui service/repository. Template `pasien/dashboard.html` wajib menggunakan `th:text`, `th:each`, dan `th:if` — tidak boleh ada data statis di HTML.

---

### 3.2 Alur Cari Dokter

**Endpoint:** `GET /pasien/cari-dokter`

**Controller:** `src/main/java/rsis/controller/PasienController.java`

**Alur:**
1. Pasien mengakses `/pasien/cari-dokter?keyword=...`
2. `PasienController.searchDoctors()` dipanggil
3. Get user dan pasien data
4. Jika keyword kosong → `PasienService.cariDokter(null)` (semua dokter)
5. Jika keyword ada → `PasienService.cariDokter(keyword)`
6. Add data ke Model
7. Render template `pasien/cari-dokter.html`

**Aturan data dinamis:** Daftar dokter beserta nama, spesialisasi, dan poli harus ditampilkan dari database. Template wajib `th:each` untuk merender kartu dokter.

**Service:** `src/main/java/rsis/service/PasienService.java`

**Alur PasienService.cariDokter():**
1. Jika keyword null/empty → return `DokterRepository.findAll()`
2. Jika keyword ada → return `DokterRepository.searchBySpesialisasiOrNama(keyword)`

**Repository yang digunakan:**
- `DokterRepository.findAll()` — get semua dokter
- `DokterRepository.searchBySpesialisasiOrNama()` — search dokter

---

### 3.3 Alur Lihat Jadwal Dokter

**Endpoint:** `GET /pasien/jadwal-dokter/{dokterId}`

**Controller:** `src/main/java/rsis/controller/PasienController.java`

**Alur:**
1. Pasien mengklik dokter dari halaman cari dokter
2. `PasienController.showDoctorSchedule()` dipanggil
3. Get user data untuk navbar
4. Get data dokter via `DokterRepository.findById(dokterId)` untuk ditampilkan di header halaman
5. Panggil `PasienService.lihatJadwalDokter(dokterId)`
6. Add data ke Model
7. Render template `pasien/jadwal-dokter.html`

**Aturan data dinamis:** Nama dokter, spesialisasi, poli, dan daftar jadwal (hari, jam, sisa kuota, status) harus berasal dari database. Tidak boleh ada nama dokter atau slot jadwal yang di-hardcode di HTML.

**Alur PasienService.lihatJadwalDokter():**
1. Return `JadwalPraktikRepository.findAvailableJadwalByDokterId(dokterId)`

**Repository yang digunakan:**
- `DokterRepository.findById()` — get data dokter
- `JadwalPraktikRepository.findAvailableJadwalByDokterId()` — get jadwal tersedia

---

### 3.4 Alur Booking Appointment

**Endpoint:** `GET /appointment/booking` dan `POST /appointment/booking`

**Controller:** `src/main/java/rsis/controller/AppointmentController.java`

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.dto.BookingRequestDTO;
import rsis.model.Appointment;
import rsis.model.AppUser;
import rsis.model.JadwalPraktik;
import rsis.repository.AppUserRepository;
import rsis.repository.JadwalPraktikRepository;
import rsis.service.AppointmentService;
import rsis.service.NotifikasiService;
import java.security.Principal;
import java.util.List;
```

**Alur GET (Show Form):**
1. Pasien mengklik tombol "Booking" dari halaman jadwal dokter
2. Redirect ke `/appointment/booking?jadwalId=...`
3. `AppointmentController.showBookingForm()` dipanggil
4. Get data jadwal via `JadwalPraktikRepository.findById(jadwalId)` untuk ditampilkan di form (nama dokter, hari, jam, kuota)
5. Get user data untuk navbar
6. Create `BookingRequestDTO` object
7. Add data ke Model
8. Render template `pasien/booking.html`

**Aturan data dinamis:** Detail jadwal yang ditampilkan di form booking (nama dokter, spesialisasi, hari, jam, kuota tersisa) harus berasal dari database. Tidak boleh di-hardcode di HTML.

**Alur POST (Submit Booking):**
1. Pasien mengisi catatan dan submit
2. `AppointmentController.bookAppointment()` dipanggil
3. Set pasienId dari principal
4. Panggil `AppointmentService.bookAppointment(bookingRequest)`
5. Jika sukses, panggil `NotifikasiService.kirimNotifikasi()` — notif booking berhasil ke pasien
6. Redirect ke `/appointment/my-appointments` dengan flash message success
7. Jika gagal, redirect ke `/appointment/booking` dengan flash message error

**Service:** `src/main/java/rsis/service/AppointmentService.java`

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.dto.BookingRequestDTO;
import rsis.model.Appointment;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
import rsis.repository.AppointmentRepository;
import rsis.repository.JadwalPraktikRepository;
import rsis.repository.PasienRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
```

**Alur AppointmentService.bookAppointment():**
1. Validate pasien via `PasienRepository.findById(pasienId)`
2. Validate profil pasien lengkap via `pasien.isProfileComplete()`
3. Validate jadwal via `JadwalPraktikRepository.findById(jadwalId)`
4. Validate jadwal tersedia via `jadwal.cekTersedia()`
5. Get dokter dari `jadwal.getDokter()`
6. Create `Appointment` entity:
   - Generate ID format `apt-{timestamp}`
   - Set pasien, dokter (dari `jadwal.getDokter()`), jadwal
   - Set tanggalBooking = LocalDate.now()
   - Set status = "MENUNGGU"
   - Set catatan dari DTO
   - Generate nomor antrian
7. Save appointment via `AppointmentRepository.save()`
8. Kurangi kuota via `jadwal.kurangiKuota()`
9. Save jadwal via `JadwalPraktikRepository.save()`
10. Return saved appointment

> **Sesuai class diagram:** Entity `Appointment` memiliki atribut `dokter : Dokter` langsung. Field ini wajib diisi dari `jadwal.getDokter()` saat booking, bukan hanya menyimpan referensi ke jadwal saja.

**Repository yang digunakan:**
- `PasienRepository.findById()` — get pasien
- `JadwalPraktikRepository.findById()` — get jadwal
- `AppointmentRepository.save()` — simpan appointment
- `JadwalPraktikRepository.save()` — update kuota

---

### 3.5 Alur Lihat Riwayat Appointment

**Endpoint:** `GET /appointment/my-appointments`

**Controller:** `src/main/java/rsis/controller/AppointmentController.java`

**Alur:**
1. Pasien mengakses `/appointment/my-appointments`
2. `AppointmentController.showMyAppointments()` dipanggil
3. Get user data untuk navbar
4. Get pasienId dari principal
5. Panggil `AppointmentService.getAppointmentsByPasienId(pasienId)`
6. Add data ke Model
7. Render template `pasien/jadwal-riwayat.html`

**Aturan data dinamis:** Semua baris riwayat (nama dokter, tanggal booking, status, nomor antrian) harus berasal dari database via `AppointmentRepository`. Template wajib `th:each`.

**Alur AppointmentService.getAppointmentsByPasienId():**
1. Return `AppointmentRepository.findByPasien_IdPasien(pasienId)`

**Repository yang digunakan:**
- `AppointmentRepository.findByPasien_IdPasien()` — get appointments pasien

---

### 3.6 Alur Cancel Appointment

**Endpoint:** `POST /appointment/cancel/{id}`

**Controller:** `src/main/java/rsis/controller/AppointmentController.java`

**Alur:**
1. Pasien mengklik tombol "Cancel" pada appointment
2. Submit form ke `POST /appointment/cancel/{id}`
3. `AppointmentController.cancelAppointment()` dipanggil
4. Panggil `AppointmentService.cancelAppointment(appointmentId)`
5. Panggil `NotifikasiService.kirimNotifikasi()` — notif pembatalan ke pasien
6. Redirect ke `/appointment/my-appointments` dengan flash message

**Alur AppointmentService.cancelAppointment():**
1. Get appointment via `AppointmentRepository.findById(appointmentId)`
2. Validate status (hanya bisa cancel jika MENUNGGU atau DIKONFIRMASI)
3. Panggil `appointment.batalkan()` → set status = "DIBATALKAN"
4. Save appointment via `AppointmentRepository.save()`
5. Restore kuota via `jadwal.tambahKuota()`
6. Save jadwal via `JadwalPraktikRepository.save()`

**Repository yang digunakan:**
- `AppointmentRepository.findById()` — get appointment
- `AppointmentRepository.save()` — update appointment
- `JadwalPraktikRepository.save()` — restore kuota

---

### 3.7 Alur Reschedule Appointment

**Endpoint:** `POST /appointment/reschedule/{id}`

**Controller:** `src/main/java/rsis/controller/AppointmentController.java`

**Alur:**
1. Pasien memilih jadwal baru dan submit
2. Submit form ke `POST /appointment/reschedule/{id}?newJadwalId=...`
3. `AppointmentController.rescheduleAppointment()` dipanggil
4. Panggil `AppointmentService.rescheduleAppointment(appointmentId, newJadwalId)`
5. Redirect ke `/appointment/my-appointments` dengan flash message

**Alur AppointmentService.rescheduleAppointment():**
1. Get appointment via `AppointmentRepository.findById(appointmentId)`
2. Validate status (hanya bisa reschedule jika MENUNGGU)
3. Restore kuota old jadwal via `oldJadwal.tambahKuota()` dan save
4. Validate new jadwal exists dan tersedia via `JadwalPraktikRepository.findById(newJadwalId)`
5. Update appointment: set jadwal baru, dokter baru dari `newJadwal.getDokter()`, dan nomor antrian baru
6. Save appointment via `AppointmentRepository.save()`
7. Kurangi kuota new jadwal via `newJadwal.kurangiKuota()` dan save

> **Catatan:** Field `dokter` di Appointment diperbarui mengikuti dokter dari jadwal baru, konsisten dengan class diagram.

**Repository yang digunakan:**
- `AppointmentRepository.findById()` — get appointment
- `JadwalPraktikRepository.findById()` — get new jadwal
- `AppointmentRepository.save()` — update appointment
- `JadwalPraktikRepository.save()` — update kuota (old dan new)

---

### 3.8 Alur Update Profil Pasien

**Endpoint:** `GET /pasien/profil` dan `POST /pasien/profil`

**Controller:** `src/main/java/rsis/controller/PasienController.java`

**Alur GET:**
1. Pasien mengakses `/pasien/profil`
2. `PasienController.profil()` dipanggil
3. Get user dan pasien data dari database
4. Add data ke Model
5. Render template `pasien/profil.html`

**Aturan data dinamis:** Form profil harus menampilkan nilai yang sudah tersimpan di database (nama, email, nomorRekamMedis, tanggalLahir, alamat) menggunakan `th:value`.

**Alur POST:**
1. Pasien mengupdate profil dan submit
2. `PasienController.updateProfile()` dipanggil
3. Panggil `PasienService.updateProfil(pasienId, nomorRekamMedis, tanggalLahir, alamat)`
4. Redirect ke `/pasien/profil` dengan flash message

**Alur PasienService.updateProfil():**
1. Get pasien via `PasienRepository.findById(pasienId)`
2. Update fields yang diberikan
3. Save via `PasienRepository.save()`
4. Return updated pasien

> **Sesuai class diagram:** `Pasien` memiliki atribut `nomorRekamMedis`, `tanggalLahir`, dan `alamat`. Field `nomorHp` tidak ada di class diagram untuk Pasien, sehingga dihapus dari parameter `updateProfil()`.

**Repository yang digunakan:**
- `PasienRepository.findById()` — get pasien
- `PasienRepository.save()` — update pasien

---

## 4. Alur Fitur Dokter

### 4.1 Alur Dashboard Dokter

**Endpoint:** `GET /dokter/dashboard`

**Controller:** `src/main/java/rsis/controller/DokterController.java`

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Appointment;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.repository.DokterRepository;
import rsis.repository.AppUserRepository;
import rsis.service.DokterService;
import java.security.Principal;
import java.util.List;
```

**Alur:**
1. Dokter yang sudah login mengakses `/dokter/dashboard`
2. Spring Security memverifikasi role DOKTER
3. `DokterController.dashboard()` dipanggil
4. Get dokterId dari principal (via `AppUserRepository` → `DokterRepository.findByEmail()`)
5. Get data dokter via `DokterRepository.findById(dokterId)` — untuk menampilkan nama, spesialisasi, poli
6. Get pending appointments via `DokterService.getPendingAppointments(dokterId)`
7. Get jadwal hari ini via `DokterService.getJadwalByDokterId(dokterId)`
8. Add semua data ke Model
9. Render template `dokter/dashboard.html`

**Aturan data dinamis:** Nama dokter, spesialisasi, jumlah pending appointment, dan jadwal hari ini harus berasal dari database. Tidak boleh ada data statis di HTML.

---

### 4.2 Alur Kelola Jadwal Dokter

**Endpoint:** `GET /dokter/jadwal`

**Controller:** `src/main/java/rsis/controller/DokterController.java`

**Alur:**
1. Dokter mengakses `/dokter/jadwal`
2. `DokterController.jadwal()` dipanggil
3. Get dokterId dari principal
4. Panggil `DokterService.getJadwalByDokterId(dokterId)`
5. Add data ke Model
6. Render template `dokter/jadwal.html`

**Aturan data dinamis:** Semua baris jadwal (hari, jam, kuota, sisa kuota, status) harus berasal dari database. Template wajib `th:each`.

**Service:** `src/main/java/rsis/service/DokterService.java`

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.Appointment;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.repository.AppointmentRepository;
import rsis.repository.DokterRepository;
import rsis.repository.JadwalPraktikRepository;
import java.util.List;
import java.util.Optional;
```

**Alur DokterService.getJadwalByDokterId():**
1. Return `JadwalPraktikRepository.findByDokter_IdDokter(dokterId)`

**Repository yang digunakan:**
- `JadwalPraktikRepository.findByDokter_IdDokter()` — get jadwal dokter

---

### 4.3 Alur Create Jadwal Dokter

**Endpoint:** `POST /dokter/jadwal/create`

**Controller:** `src/main/java/rsis/controller/DokterController.java`

**Alur:**
1. Dokter mengisi form jadwal baru dan submit
2. `DokterController.createJadwal()` dipanggil
3. Set dokter dari principal (via DokterRepository)
4. Panggil `DokterService.createJadwal(jadwal)`
5. Redirect ke `/dokter/jadwal` dengan flash message

**Alur DokterService.createJadwal():**
1. Generate jadwal ID format `jdw-{timestamp}`
2. Set idJadwal, statusKetersediaan = "TERSEDIA", sisaKuota = kuota
3. Save via `JadwalPraktikRepository.save()`
4. Return saved jadwal

> **Sesuai ISchedulable:** Method ini merupakan implementasi `updateJadwal()` dari interface `ISchedulable` yang diimplementasi oleh `Dokter`.

**Repository yang digunakan:**
- `JadwalPraktikRepository.save()` — simpan jadwal baru

---

### 4.4 Alur Update Jadwal Dokter

**Endpoint:** `POST /dokter/jadwal/update`

**Controller:** `src/main/java/rsis/controller/DokterController.java`

**Alur:**
1. Dokter mengupdate jadwal dan submit
2. `DokterController.updateJadwal()` dipanggil
3. Panggil `DokterService.updateJadwal(jadwal)`
4. Redirect ke `/dokter/jadwal` dengan flash message

**Alur DokterService.updateJadwal():**
1. Validate jadwal exists via `JadwalPraktikRepository.findById(jadwal.getIdJadwal())`
2. Save via `JadwalPraktikRepository.save()`
3. Return updated jadwal

**Repository yang digunakan:**
- `JadwalPraktikRepository.save()` — update jadwal

---

### 4.5 Alur Delete Jadwal Dokter

**Endpoint:** `POST /dokter/jadwal/delete/{id}`

**Controller:** `src/main/java/rsis/controller/DokterController.java`

**Alur:**
1. Dokter mengklik tombol delete jadwal
2. `DokterController.deleteJadwal()` dipanggil
3. Validate tidak ada appointment MENUNGGU/DIKONFIRMASI pada jadwal ini
4. Panggil `DokterService.deleteJadwal(jadwalId)`
5. Redirect ke `/dokter/jadwal` dengan flash message

**Alur DokterService.deleteJadwal():**
1. Cek appointment aktif via `AppointmentRepository.findByJadwal_IdJadwalAndStatusIn(jadwalId, ["MENUNGGU","DIKONFIRMASI"])`
2. Jika ada appointment aktif, throw exception dengan pesan error
3. Delete via `JadwalPraktikRepository.deleteById(jadwalId)`

**Repository yang digunakan:**
- `AppointmentRepository` — cek appointment aktif
- `JadwalPraktikRepository.deleteById()` — hapus jadwal

---

### 4.6 Alur Lihat Daftar Pasien

**Endpoint:** `GET /dokter/daftar-pasien`

**Controller:** `src/main/java/rsis/controller/DokterController.java`

**Alur:**
1. Dokter mengakses `/dokter/daftar-pasien`
2. `DokterController.daftarPasien()` dipanggil
3. Get dokterId dari principal
4. Panggil `DokterService.getDaftarPasien(dokterId)`
5. Add data ke Model
6. Render template `dokter/daftar-pasien.html`

**Aturan data dinamis:** Semua baris daftar pasien (nama pasien, jadwal, status, nomor antrian) harus berasal dari database via `AppointmentRepository`.

**Alur DokterService.getDaftarPasien():**
1. Return `AppointmentRepository.findByDokter_IdDokter(dokterId)`

> **Sesuai class diagram:** Query menggunakan field `dokter` langsung di Appointment (bukan melalui jadwal), karena Appointment memiliki relasi langsung ke Dokter.

**Repository yang digunakan:**
- `AppointmentRepository.findByDokter_IdDokter()` — get appointments dokter

---

### 4.7 Alur Lihat Pending Appointments

**Endpoint:** `GET /dokter/appointment/pending`

**Controller:** `src/main/java/rsis/controller/DokterController.java`

**Alur:**
1. Dokter mengakses `/dokter/appointment/pending`
2. `DokterController.pendingAppointments()` dipanggil
3. Get dokterId dari principal
4. Panggil `DokterService.getPendingAppointments(dokterId)`
5. Add data ke Model
6. Render template `dokter/appointment-pending.html`

**Aturan data dinamis:** Semua pending appointment (nama pasien, jadwal, catatan pasien) harus berasal dari database.

**Alur DokterService.getPendingAppointments():**
1. Return `AppointmentRepository.findByDokter_IdDokterAndStatus(dokterId, "MENUNGGU")`

**Repository yang digunakan:**
- `AppointmentRepository.findByDokter_IdDokterAndStatus()` — get pending appointments

---

### 4.8 Alur Konfirmasi Appointment

**Endpoint:** `POST /dokter/appointment/konfirmasi/{id}`

**Controller:** `src/main/java/rsis/controller/DokterController.java`

**Alur:**
1. Dokter mengklik tombol "Konfirmasi"
2. `DokterController.konfirmasiAppointment()` dipanggil
3. Panggil `DokterService.konfirmasiAppointment(appointmentId)`
4. Panggil `NotifikasiService.kirimNotifikasi()` — notif konfirmasi ke pasien
5. Redirect ke `/dokter/appointment/pending` dengan flash message

**Alur DokterService.konfirmasiAppointment():**
1. Get appointment via `AppointmentRepository.findById(appointmentId)`
2. Panggil `appointment.konfirmasi()` → set status = "DIKONFIRMASI"
3. Save via `AppointmentRepository.save()`

**Repository yang digunakan:**
- `AppointmentRepository.findById()` — get appointment
- `AppointmentRepository.save()` — update appointment

---

### 4.9 Alur Tolak Appointment

**Endpoint:** `POST /dokter/appointment/tolak/{id}`

**Controller:** `src/main/java/rsis/controller/DokterController.java`

**Alur:**
1. Dokter mengisi alasan penolakan dan submit
2. Submit form ke `POST /dokter/appointment/tolak/{id}?alasan=...`
3. `DokterController.tolakAppointment()` dipanggil
4. Panggil `DokterService.tolakAppointment(appointmentId, alasan)`
5. Panggil `NotifikasiService.kirimNotifikasi()` — notif penolakan ke pasien
6. Redirect ke `/dokter/appointment/pending` dengan flash message

**Alur DokterService.tolakAppointment():**
1. Get appointment via `AppointmentRepository.findById(appointmentId)`
2. Panggil `appointment.tolak(alasan)` → set status = "DITOLAK", set alasanTolak
3. Save via `AppointmentRepository.save()`
4. Restore kuota jadwal via `jadwal.tambahKuota()`
5. Save jadwal via `JadwalPraktikRepository.save()`

**Repository yang digunakan:**
- `AppointmentRepository.findById()` — get appointment
- `AppointmentRepository.save()` — update appointment
- `JadwalPraktikRepository.save()` — restore kuota

---

## 5. Alur Fitur Admin

### 5.1 Alur Dashboard Admin

**Endpoint:** `GET /admin/dashboard`

**Controller:** `src/main/java/rsis/controller/AdminController.java`

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Dokter;
import rsis.model.Poli;
import rsis.model.Spesialisasi;
import rsis.service.AdminRSService;
import java.util.List;
import java.util.Map;
```

**Alur:**
1. Admin yang sudah login mengakses `/admin/dashboard`
2. Spring Security memverifikasi role ADMIN
3. `AdminController.dashboard()` dipanggil
4. Panggil `AdminRSService.getTotalPasienHariIni()`
5. Panggil `AdminRSService.getTotalPasienBulanIni()`
6. Panggil `AdminRSService.getDokterTersibuk()`
7. Panggil `AdminRSService.getPasienPerHari()`
8. Panggil `AdminRSService.getTotalDokter()`
9. Panggil `AdminRSService.getTotalPoli()`
10. Add semua data ke Model
11. Render template `admin/dashboard.html`

**Aturan data dinamis:** Semua angka statistik (total pasien hari ini, bulan ini, dokter tersibuk, grafik pasien per hari) harus berasal dari database. Tidak boleh ada angka statis di HTML.

**Service:** `src/main/java/rsis/service/AdminRSService.java`

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Poli;
import rsis.model.Spesialisasi;
import rsis.repository.AppointmentRepository;
import rsis.repository.DokterRepository;
import rsis.repository.JadwalPraktikRepository;
import rsis.repository.PoliRepository;
import rsis.repository.SpesialisasiRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
```

> **Sesuai class diagram:** Method statistik (`getTotalPasienHariIni()`, `getTotalPasienBulanIni()`, `getDokterTersibuk()`, `getPasienPerHari()`) berada di `AdminRS`. Dalam implementasi Spring Boot, method ini diletakkan di `AdminRSService` sebagai service layer untuk `AdminRS`. Class `DashboardStatistik` dari proposal **dihapus** karena tidak ada di class diagram.

**Alur AdminRSService.getTotalPasienHariIni():**
1. Get today's date
2. Return `AppointmentRepository.countConfirmedAppointmentsByDate(today)`

**Alur AdminRSService.getTotalPasienBulanIni():**
1. Get current month/year
2. Return `AppointmentRepository.countConfirmedAppointmentsByMonth(bulan, tahun)`

**Alur AdminRSService.getDokterTersibuk():**
1. Return `AppointmentRepository.findBusiestDokterByMonth(bulan, tahun)`

**Alur AdminRSService.getPasienPerHari():**
1. Return `AppointmentRepository.findPatientsPerDayByMonth(bulan, tahun)` sebagai `Map<String, Integer>`

**Repository yang digunakan:**
- `AppointmentRepository.countConfirmedAppointmentsByDate()` — count hari ini
- `AppointmentRepository.countConfirmedAppointmentsByMonth()` — count bulan ini
- `AppointmentRepository.findBusiestDokterByMonth()` — dokter tersibuk
- `AppointmentRepository.findPatientsPerDayByMonth()` — pasien per hari
- `DokterRepository.count()` — total dokter
- `PoliRepository.count()` — total poli

---

### 5.2 Alur Kelola Dokter

**Endpoint:** `GET /admin/kelola-dokter`

**Controller:** `src/main/java/rsis/controller/AdminController.java`

**Alur:**
1. Admin mengakses `/admin/kelola-dokter`
2. `AdminController.kelolaDokter()` dipanggil
3. Panggil `AdminRSService.getAllDokter()`
4. Panggil `AdminRSService.getAllPoli()` dan `AdminRSService.getAllSpesialisasi()` untuk dropdown form tambah dokter
5. Add data ke Model
6. Render template `admin/kelola-dokter.html`

**Aturan data dinamis:** Tabel dokter dan dropdown poli/spesialisasi di form harus berasal dari database. Tidak boleh ada nama dokter atau pilihan dropdown yang di-hardcode di HTML.

---

### 5.3 Alur Create Dokter

**Endpoint:** `POST /admin/dokter/create`

**Controller:** `src/main/java/rsis/controller/AdminController.java`

**Alur:**
1. Admin mengisi form dokter baru dan submit
2. `AdminController.createDokter()` dipanggil
3. Panggil `AdminRSService.createDokter(dokter, idPoli, idSpesialisasi)`
4. Redirect ke `/admin/kelola-dokter` dengan flash message

**Alur AdminRSService.createDokter():**
1. Generate dokter ID format `dkt-XXXX` via `DokterRepository.findLatestDokterId()`
2. Generate user ID format `usr-XXX` via `AppUserRepository.findLatestUserId()`
3. Buat `AppUser` entity dengan role="DOKTER" dan password di-encode
4. Simpan `AppUser` via `AppUserRepository.save()`
5. Get `Poli` via `PoliRepository.findById(idPoli)`
6. Get `Spesialisasi` via `SpesialisasiRepository.findById(idSpesialisasi)`
7. Set idDokter, poli, spesialisasi ke entity Dokter
8. Save via `DokterRepository.save()`
9. Return saved dokter

**Repository yang digunakan:**
- `AppUserRepository.findLatestUserId()` — generate user ID
- `AppUserRepository.save()` — simpan AppUser dokter
- `DokterRepository.findLatestDokterId()` — generate dokter ID
- `PoliRepository.findById()` — get poli
- `SpesialisasiRepository.findById()` — get spesialisasi
- `DokterRepository.save()` — simpan dokter baru

---

### 5.4 Alur Update Dokter

**Endpoint:** `POST /admin/dokter/update`

**Controller:** `src/main/java/rsis/controller/AdminController.java`

**Alur:**
1. Admin mengupdate data dokter dan submit
2. `AdminController.updateDokter()` dipanggil
3. Panggil `AdminRSService.updateDokter(dokter)`
4. Redirect ke `/admin/kelola-dokter` dengan flash message

**Alur AdminRSService.updateDokter():**
1. Get existing dokter via `DokterRepository.findById(dokter.getIdDokter())`
2. Update field nomorStr, poli, spesialisasi
3. Save via `DokterRepository.save()`
4. Return updated dokter

**Repository yang digunakan:**
- `DokterRepository.findById()` — get dokter existing
- `DokterRepository.save()` — update dokter

---

### 5.5 Alur Delete Dokter

**Endpoint:** `POST /admin/dokter/delete/{id}`

**Controller:** `src/main/java/rsis/controller/AdminController.java`

**Alur:**
1. Admin mengklik tombol delete dokter
2. `AdminController.deleteDokter()` dipanggil
3. Panggil `AdminRSService.deleteDokter(dokterId)`
4. Redirect ke `/admin/kelola-dokter` dengan flash message

**Alur AdminRSService.deleteDokter():**
1. Delete jadwal dokter via `JadwalPraktikRepository.deleteByDokter_IdDokter(dokterId)`
2. Delete via `DokterRepository.deleteById(dokterId)`

**Repository yang digunakan:**
- `JadwalPraktikRepository.deleteByDokter_IdDokter()` — hapus jadwal dokter terlebih dahulu
- `DokterRepository.deleteById()` — hapus dokter

---

### 5.6 Alur Kelola Poli

**Endpoint:** `GET /admin/kelola-poli`

**Controller:** `src/main/java/rsis/controller/AdminController.java`

**Alur:**
1. Admin mengakses `/admin/kelola-poli`
2. `AdminController.kelolaPoli()` dipanggil
3. Panggil `AdminRSService.getAllPoli()`
4. Add data ke Model
5. Render template `admin/kelola-poli.html`

**Aturan data dinamis:** Tabel poli (nama poli, lokasi ruangan) harus berasal dari database.

**Alur AdminRSService.getAllPoli():**
1. Return `PoliRepository.findAll()`

---

### 5.7 Alur Create Poli

**Endpoint:** `POST /admin/poli/create`

**Alur AdminRSService.createPoli():**
1. Generate poli ID format `pli-XXXX` via `PoliRepository.findLatestPoliId()`
2. Set idPoli
3. Save via `PoliRepository.save()`
4. Return saved poli

**Repository yang digunakan:**
- `PoliRepository.findLatestPoliId()` — generate poli ID
- `PoliRepository.save()` — simpan poli baru

---

### 5.8 Alur Update Poli

**Endpoint:** `POST /admin/poli/update`

**Alur AdminRSService.updatePoli():**
1. Validate via `PoliRepository.findById()`
2. Save via `PoliRepository.save()`

**Repository yang digunakan:**
- `PoliRepository.save()` — update poli

---

### 5.9 Alur Delete Poli

**Endpoint:** `POST /admin/poli/delete/{id}`

**Alur AdminRSService.deletePoli():**
1. Cek apakah ada dokter yang terdaftar di poli ini via `DokterRepository.findByPoli_IdPoli(poliId)`
2. Jika ada dokter, throw exception dengan pesan error
3. Delete via `PoliRepository.deleteById(poliId)`

**Repository yang digunakan:**
- `DokterRepository.findByPoli_IdPoli()` — cek dokter di poli
- `PoliRepository.deleteById()` — hapus poli

---

### 5.10 Alur Kelola Spesialisasi

**Endpoint:** `GET /admin/kelola-spesialisasi`

**Alur:**
1. Admin mengakses `/admin/kelola-spesialisasi`
2. `AdminController.kelolaSpesialisasi()` dipanggil
3. Panggil `AdminRSService.getAllSpesialisasi()`
4. Add data ke Model
5. Render template `admin/kelola-spesialisasi.html`

**Aturan data dinamis:** Tabel spesialisasi (nama, deskripsi) harus berasal dari database.

**Alur AdminRSService.getAllSpesialisasi():**
1. Return `SpesialisasiRepository.findAll()`

---

### 5.11 Alur Create Spesialisasi

**Endpoint:** `POST /admin/spesialisasi/create`

**Alur AdminRSService.createSpesialisasi():**
1. Generate ID via `SpesialisasiRepository.findLatestSpesialisasiId()`
2. Save via `SpesialisasiRepository.save()`
3. Return saved spesialisasi

---

### 5.12 Alur Delete Spesialisasi

**Endpoint:** `POST /admin/spesialisasi/delete/{id}`

**Alur AdminRSService.deleteSpesialisasi():**
1. Cek apakah ada dokter dengan spesialisasi ini via `DokterRepository.findBySpesialisasi_IdSpesialisasi(id)`
2. Jika ada dokter, throw exception dengan pesan error
3. Delete via `SpesialisasiRepository.deleteById(id)`

**Repository yang digunakan:**
- `DokterRepository.findBySpesialisasi_IdSpesialisasi()` — cek dokter dengan spesialisasi ini
- `SpesialisasiRepository.deleteById()` — hapus spesialisasi

---

### 5.13 Alur Kelola Jadwal (Admin)

**Endpoint:** `GET /admin/kelola-jadwal`

**Alur:**
1. Admin mengakses `/admin/kelola-jadwal`
2. `AdminController.kelolaJadwal()` dipanggil
3. Panggil `AdminRSService.getAllJadwal()`
4. Panggil `AdminRSService.getAllDokter()` untuk dropdown filter
5. Add data ke Model
6. Render template `admin/kelola-jadwal.html`

**Aturan data dinamis:** Semua jadwal dari semua dokter harus ditampilkan dari database. Tidak boleh ada data jadwal statis di HTML.

> **Perubahan:** Fitur kelola jadwal untuk admin ditangani langsung oleh `AdminController` (endpoint `/admin/kelola-jadwal`), bukan lagi didelegasikan ke `JadwalController`. `JadwalController` terpisah dihapus karena menyebabkan tumpang tindih tanggung jawab.

**Alur AdminRSService.getAllJadwal():**
1. Return `JadwalPraktikRepository.findAll()`

---

### 5.14 Alur Laporan Bulanan

**Endpoint:** `GET /admin/laporan-bulanan`

**Alur:**
1. Admin mengakses `/admin/laporan-bulanan`
2. `AdminController.laporanBulanan()` dipanggil
3. Redirect ke `/laporan` (delegasi ke LaporanController)

---

## 6. Alur Fitur Laporan

### 6.1 Alur Index Laporan

**Endpoint:** `GET /laporan`

**Controller:** `src/main/java/rsis/controller/LaporanController.java`

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rsis.model.LaporanBulanan;
import rsis.service.LaporanBulananService;
import java.time.LocalDate;
```

**Alur:**
1. Admin mengakses `/laporan`
2. `LaporanController.laporanIndex()` dipanggil
3. Get current month and year
4. Panggil `LaporanBulananService.generate(bulan, tahun)` untuk generate object `LaporanBulanan`
5. Add data ke Model
6. Render template `laporan/index.html`

**Aturan data dinamis:** Semua angka di halaman laporan (total pasien, total appointment, total batal) harus berasal dari database via `LaporanBulananService`.

**Service:** `src/main/java/rsis/service/LaporanBulananService.java`

```java
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rsis.model.Appointment;
import rsis.model.LaporanBulanan;
import rsis.repository.AppointmentRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
```

> **Sesuai class diagram:** `LaporanBulanan` adalah class dengan method `generate()`, `exportPDF()`, dan `exportCSV()`. Dalam implementasi Spring Boot, `LaporanBulananService` bertanggung jawab membuat dan mengisi object `LaporanBulanan` dari data database, lalu memanggil method-method tersebut.

**Alur LaporanBulananService.generate(bulan, tahun):**
1. Buat object `LaporanBulanan` baru
2. Set bulan dan tahun
3. Count total pasien (confirmed) via `AppointmentRepository.countConfirmedAppointmentsByMonth(bulan, tahun)` → set `totalPasien`
4. Count total appointment (semua status) via `AppointmentRepository.countAllByMonth(bulan, tahun)` → set `totalAppointment`
5. Count total batal via `AppointmentRepository.countCanceledAppointmentsByMonth(bulan, tahun)` → set `totalBatal`
6. Return object `LaporanBulanan`

**Repository yang digunakan:**
- `AppointmentRepository.countConfirmedAppointmentsByMonth()` — total pasien
- `AppointmentRepository.countAllByMonth()` — total appointment
- `AppointmentRepository.countCanceledAppointmentsByMonth()` — total batal

---

### 6.2 Alur Laporan Bulanan dengan Filter

**Endpoint:** `GET /laporan/bulanan`

**Controller:** `src/main/java/rsis/controller/LaporanController.java`

**Alur:**
1. Admin mengakses `/laporan/bulanan?bulan=...&tahun=...`
2. `LaporanController.laporanBulanan()` dipanggil
3. Get bulan dan tahun dari parameter (default current month/year)
4. Panggil `LaporanBulananService.generate(bulan, tahun)`
5. Add data ke Model
6. Render template `laporan/bulanan.html`

---

### 6.3 Alur Export PDF

**Endpoint:** `GET /laporan/export/pdf`

**Controller:** `src/main/java/rsis/controller/LaporanController.java`

**Alur:**
1. Admin mengklik tombol "Export PDF"
2. `LaporanController.exportPDF(bulan, tahun)` dipanggil
3. Panggil `LaporanBulananService.generate(bulan, tahun)` untuk generate object `LaporanBulanan`
4. Panggil `laporan.exportPDF()` — method dari class `LaporanBulanan`
5. Return `ResponseEntity` dengan PDF file sebagai attachment

**Alur LaporanBulanan.exportPDF():**
1. Buat konten PDF dari field `totalPasien`, `totalAppointment`, `totalBatal`, `bulan`, `tahun`
2. Return `File` hasil export

---

### 6.4 Alur Export CSV

**Endpoint:** `GET /laporan/export/csv`

**Controller:** `src/main/java/rsis/controller/LaporanController.java`

**Alur:**
1. Admin mengklik tombol "Export CSV"
2. `LaporanController.exportCSV(bulan, tahun)` dipanggil
3. Panggil `LaporanBulananService.generateCSV(bulan, tahun)`
4. Return `ResponseEntity` dengan CSV file sebagai attachment

**Alur LaporanBulananService.generateCSV(bulan, tahun):**
1. Get appointments berdasarkan bulan dan tahun via `AppointmentRepository.findByBulanDanTahun(bulan, tahun)` — **bukan** `findAll()`
2. Create CSVPrinter dengan header: ID Appointment, Tanggal Booking, Pasien, Dokter, Status
3. Loop appointments dan print ke CSV
4. Return byte array dari CSV

> **Perbaikan bug:** Export CSV sebelumnya menggunakan `AppointmentRepository.findAll()` tanpa filter bulan/tahun sehingga mengeksport semua data. Sekarang menggunakan query dengan filter bulan dan tahun yang sesuai parameter laporan.

**Repository yang digunakan:**
- `AppointmentRepository.findByBulanDanTahun(bulan, tahun)` — get appointments sesuai periode

---

## 7. Fungsi Setiap File di src/main

### 7.1 Entry Point

**RsisApplication.java**
- Fungsi: Entry point aplikasi Spring Boot
- Menginisialisasi Spring Boot application context dan embedded web server

### 7.2 Config Package

**SecurityConfig.java**
- Mengatur URL permissions per role
- Mengkonfigurasi form login/logout dan BCrypt password encoder
- Mengatur authentication success handler untuk redirect berdasarkan role

**ThymeleafConfig.java**
- Mengkonfigurasi Thymeleaf template resolver (prefix, suffix, cache off)
- Menambahkan Spring Security dialect

**AuthBeansConfig.java**
- Mengkonfigurasi `AuthenticationManager` bean

### 7.3 Controller Package

**AuthController.java**
- Endpoints: `/`, `/auth`, `/login`, `/register`
- Menampilkan form login dan register
- Memproses registrasi pasien baru

**PasienController.java**
- Endpoints: `/pasien/dashboard`, `/pasien/profil`, `/pasien/cari-dokter`, `/pasien/jadwal-dokter/{dokterId}`
- Menampilkan dashboard, profil, pencarian dokter, jadwal dokter

**DokterController.java**
- Endpoints: `/dokter/dashboard`, `/dokter/jadwal`, `/dokter/jadwal/create`, `/dokter/jadwal/update`, `/dokter/jadwal/delete/{id}`, `/dokter/daftar-pasien`, `/dokter/appointment/pending`, `/dokter/appointment/konfirmasi/{id}`, `/dokter/appointment/tolak/{id}`
- Mengelola seluruh fitur dokter termasuk CRUD jadwal dan manajemen appointment

**AdminController.java**
- Endpoints: `/admin/dashboard`, `/admin/kelola-dokter`, `/admin/kelola-poli`, `/admin/kelola-spesialisasi`, `/admin/kelola-jadwal`
- CRUD dokter, poli, spesialisasi, dan jadwal
- Dashboard statistik dan delegasi laporan ke LaporanController

**AppointmentController.java**
- Endpoints: `/appointment/booking`, `/appointment/my-appointments`, `/appointment/cancel/{id}`, `/appointment/reschedule/{id}`
- Booking, riwayat, cancel, dan reschedule appointment

**LaporanController.java**
- Endpoints: `/laporan`, `/laporan/bulanan`, `/laporan/export/pdf`, `/laporan/export/csv`
- Menampilkan dan mengeksport laporan bulanan

> **Perubahan:** `JadwalController` dihapus. Manajemen jadwal oleh dokter dilakukan di `DokterController` (`/dokter/jadwal/*`), dan oleh admin dilakukan di `AdminController` (`/admin/kelola-jadwal`). Ini menghilangkan tumpang tindih endpoint dan memperjelas tanggung jawab masing-masing controller.

### 7.4 Service Package

**AuthService.java**
- Method: `registerPasien(nama, email, password)`
- Validasi email dan password, generate ID, membuat AppUser dan Pasien, menyimpan ke database

**UserService.java**
- Method: `loadUserByUsername(email)` — implementasi `UserDetailsService`
- Mengambil user dari `AppUserRepository` dan convert ke `UserDetails`

**PasienService.java**
- Method: `cariDokter(keyword)`, `lihatJadwalDokter(dokterId)`, `updateProfil(...)`

**DokterService.java**
- Method: `getJadwalByDokterId()`, `createJadwal()`, `updateJadwal()`, `deleteJadwal()`, `getDaftarPasien()`, `getPendingAppointments()`, `konfirmasiAppointment()`, `tolakAppointment()`

**AdminRSService.java**
- Method: `createDokter()`, `updateDokter()`, `deleteDokter()`, `getAllDokter()`, `createPoli()`, `updatePoli()`, `deletePoli()`, `getAllPoli()`, `createSpesialisasi()`, `deleteSpesialisasi()`, `getAllSpesialisasi()`, `getAllJadwal()`, `getTotalPasienHariIni()`, `getTotalPasienBulanIni()`, `getDokterTersibuk()`, `getPasienPerHari()`

> **Perubahan:** `DashboardStatistik` sebagai class/service terpisah dihapus. Method statistik sekarang berada langsung di `AdminRSService` sesuai class diagram yang menempatkan method-method tersebut di `AdminRS`.

**AppointmentService.java**
- Method: `bookAppointment()`, `cancelAppointment()`, `rescheduleAppointment()`, `getAppointmentsByPasienId()`, `getAppointmentsByDokterId()`
- Validasi pasien dan jadwal, create appointment dengan field `dokter` langsung (sesuai class diagram)

**NotifikasiService.java**
- Method: `kirimNotifikasi()`, `getNotifikasiByPenerimaId()`, `tandaiDibaca()`, `deleteNotifikasi()`

**LaporanBulananService.java**
- Method: `generate(bulan, tahun)`, `generateCSV(bulan, tahun)`
- Generate object `LaporanBulanan` dari data database, export CSV dengan filter bulan/tahun

> **Perubahan:** `SpesialisasiService` yang kosong (placeholder) dihapus. Logika spesialisasi cukup ditangani oleh `AdminRSService` melalui `SpesialisasiRepository`.

### 7.5 DTO Package

**BookingRequestDTO.java**
- Fields: `jadwalId`, `pasienId`, `catatan`

**StatistikDTO.java** *(dihapus)*
- Tidak lagi digunakan karena logika statistik dipindahkan langsung ke method-method di `AdminRSService` yang mengembalikan nilai primitif/map, lalu dimasukkan ke Model secara individual.

> **Perubahan:** `StatistikDTO` dihapus karena menyebabkan lapisan abstraksi yang tidak perlu. Data statistik langsung dimasukkan ke Model dari `AdminRSService`.

### 7.6 Model Package

**User.java** *(abstract, MappedSuperclass)*
- Fields: `idUser`, `nama`, `email`, `password`, `nomorHp`, `role`
- Abstract methods: `getEmail()`, `getNama()`, `getPassword()`, `getNomorHp()`, `getRole()`, `getId()`, `getNama()`
- Implementasi `INotifiable` interface
- Constructor: `User(idUser, nama, email, password, nomorHp, role)`

**AppUser.java**
- Entity untuk tabel `users` — digunakan untuk autentikasi Spring Security
- Fields: `idUser`, `nama`, `email`, `password`, `nomorHp`, `role`, `createdAt`

**Pasien.java** *(extends User)*
- Entity untuk tabel `pasien`
- Fields: `idPasien`, `nomorRekamMedis`, `tanggalLahir`, `alamat`
- Method: `isProfileComplete()`, `cariDokter()`, `lihatJadwalDokter()`, `bookingAppointment()`, `batalkanAppointment()`, `rescheduleAppointment()`, `getAppointmentList()`
- Relasi: tidak ada field `nomorHp` tambahan (sudah ada di parent `User`)

**Dokter.java** *(extends User, implements ISchedulable)*
- Entity untuk tabel `dokter`
- Fields: `idDokter`, `nomorStr`, `spesialisasi` (ManyToOne), `poli` (ManyToOne)
- Method: `kelolaJadwal()`, `lihatDaftarPasien()`, `konfirmasiAppointment()`, `tolakAppointment()`, `getJadwalPraktik()`, `getPoli()`, `getSpesialisasi()`

**AdminRS.java** *(extends User)*
- Entity untuk tabel `admin_rs`
- Fields: `idAdmin`, `jabatan`
- Method: `kelolaDataDokter()`, `kelolaDataPoli()`, `kelolaJadwal()`, `cetakLaporanBulanan()`, `getTotalPasienHariIni()`, `getTotalPasienBulanIni()`, `getDokterTersibuk()`, `getPasienPerHari()`

**Poli.java**
- Entity untuk tabel `poli`
- Fields: `idPoli`, `namaPoli`, `lokasiRuangan`
- Method: `getDokterList()`, `getJadwalList()`, `getNamaPoli()`, `tambahDokter()`, `hapusDokter()`
- Relasi: `OneToMany` ke Dokter

**Spesialisasi.java**
- Entity untuk tabel `spesialisasi`
- Fields: `idSpesialisasi`, `nama`, `deskripsi`
- Method: `getDokterBySpesialisasi()`, `getNama()`, `getDeskripsi()`
- Relasi: `OneToMany` ke Dokter

**JadwalPraktik.java**
- Entity untuk tabel `jadwal_praktik`
- Fields: `idJadwalPraktik`, `dokter` (ManyToOne), `hari`, `tanggal` (Date), `jamMulai`, `jamSelesai`, `statusKetersediaan`, `kuota`, `sisaKuota`
- Method: `getDokter()`, `getTanggal()`, `updateStatus(status)`, `cekTersedia()`, `tambahKuota()`, `kurangiKuota()`

**Appointment.java**
- Entity untuk tabel `appointment`
- Fields: `idAppointment`, `pasien` (ManyToOne), `dokter` (ManyToOne), `jadwal` (ManyToOne), `tanggalBooking`, `status`, `nomorAntrian`, `catatan`, `alasanTolak`
- Method: `konfirmasi()`, `batalkan()`, `ubahJadwal(jadwalBaruId)`, `tolak(alasan)`, `getStatus()`, `getPasien()`, `getDokter()`

> **Sesuai class diagram:** `Appointment` memiliki relasi langsung ke `Dokter` (field `dokter : Dokter`), bukan hanya melalui `JadwalPraktik`. Ini penting untuk query `findByDokter_IdDokter()` yang dibutuhkan oleh fitur daftar pasien dan pending appointments dokter.

**Notifikasi.java**
- Entity untuk tabel `notifikasi`
- Fields: `idNotifikasi`, `penerima` (ManyToOne ke `AppUser` yang implements `INotifiable`), `pesan`, `tipe`, `status`, `tanggalKirim`
- Method: `kirim()`, `getStatus()`, `markAsRead()`

> **Sesuai class diagram:** Field `penerima` bertipe `INotifiable`. Dalam implementasi JPA, field ini di-map ke `User` yang mengimplementasi `INotifiable`. Tidak disimpan sebagai raw FK ke tabel yang berbeda-beda.

**LaporanBulanan.java**
- Bukan entity database — class untuk generate laporan
- Fields: `idLaporan`, `bulan`, `tahun`, `totalPasien`, `totalAppointment`, `totalBatal`
- Method: `generate()`, `exportPDF()`, `exportCSV()`, `getSummary()`

### 7.7 Model Interfaces Package

**INotifiable.java**
- Method: `terimaNotifikasi(Notifikasi notif)`, `getEmail()`
- Diimplementasi oleh `User` (sehingga seluruh turunannya: Pasien, Dokter, AdminRS)

**ISchedulable.java**
- Method: `getJadwal()`, `updateJadwal(jadwal)`, `cekKetersediaan(jadwalId)`
- Diimplementasi oleh `Dokter`

### 7.8 Repository Package

**AppUserRepository.java**
- Method: `findByEmailIgnoreCase()`, `existsByEmailIgnoreCase()`, `findLatestUserId()`
- Menggabungkan semua query user — `UserRepository` yang terpisah dihapus

**PasienRepository.java**
- Method: `findByEmail()`, `findLatestPasienId()`

**DokterRepository.java**
- Method: `findBySpesialisasi_Nama()`, `findBySpesialisasi_IdSpesialisasi()`, `findByPoli_IdPoli()`, `findByEmail()`, `searchBySpesialisasiOrNama()`, `findLatestDokterId()`

**AdminRSRepository.java**
- Method: `findByEmail()`

**PoliRepository.java**
- Method: `findByNamaContainingIgnoreCase()`, `findLatestPoliId()`

**SpesialisasiRepository.java**
- Method: `findByNamaContainingIgnoreCase()`, `findByNama()`, `findLatestSpesialisasiId()`

**JadwalPraktikRepository.java**
- Method: `findByDokter_IdDokter()`, `findByDokter_Poli_IdPoli()`, `findByHari()`, `findAvailableJadwalByDokterId()`, `findAllAvailableJadwal()`, `deleteByDokter_IdDokter()`

**AppointmentRepository.java**
- Method: `findByPasien_IdPasien()`, `findByDokter_IdDokter()`, `findByDokter_IdDokterAndStatus()`, `findByStatus()`, `findByBulanDanTahun()`, `countConfirmedAppointmentsByDate()`, `countConfirmedAppointmentsByMonth()`, `countAllByMonth()`, `countCanceledAppointmentsByMonth()`, `findBusiestDokterByMonth()`, `findPatientsPerDayByMonth()`

> **Perubahan:** Method `findByJadwal_Dokter_IdDokter()` diganti dengan `findByDokter_IdDokter()` (lebih efisien karena Appointment sekarang memiliki FK langsung ke dokter). Ditambahkan `findByBulanDanTahun()` dan `countAllByMonth()` untuk kebutuhan laporan. `findPendingAppointmentsByDokterId()` diganti dengan `findByDokter_IdDokterAndStatus()` yang lebih generik.

**NotifikasiRepository.java**
- Method: `findByPenerima_IdUserOrderByTanggalKirimDesc()`

---

## 8. Summary Arsitektur

### 8.1 Layer Architecture

```
┌─────────────────────────────────────────┐
│         Presentation Layer               │
│  (Controllers + Thymeleaf Templates)    │
│  * Semua data ditampilkan dinamis dari  │
│    Model — tidak ada data statis di HTML│
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         Business Logic Layer            │
│         (Services + Models)             │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         Data Access Layer               │
│         (Repositories)                  │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         Database Layer                  │
│  (PostgreSQL/Supabase Tables)           │
└─────────────────────────────────────────┘
```

### 8.2 Security Flow

```
User Request → Security Filter Chain → Authentication Check
                                              ↓
                                    Role-based Authorization
                                              ↓
                                    Controller → Service → Repository → DB
```

### 8.3 OOP Mapping ke Spring Boot

| Konsep OOP         | Implementasi                                                                 |
|--------------------|------------------------------------------------------------------------------|
| Abstract Class User | `@MappedSuperclass` — Pasien, Dokter, AdminRS extends User                  |
| Inheritance        | Pasien, Dokter, AdminRS extends User                                         |
| Interface INotifiable | User implements INotifiable — semua user bisa terima notifikasi           |
| Interface ISchedulable | Dokter implements ISchedulable — dokter kelola jadwal                    |
| Encapsulation      | Semua atribut `private`, akses via getter/setter                             |
| Composition        | Appointment memiliki Pasien, Dokter, JadwalPraktik langsung                 |

### 8.4 Data Flow Example (Booking Appointment)

```
1. Pasien submit form booking
   ↓
2. AppointmentController.bookAppointment()
   ↓
3. AppointmentService.bookAppointment()
   - Validate pasien exists (PasienRepository)
   - Validate profil lengkap (pasien.isProfileComplete())
   - Validate jadwal exists & available (JadwalPraktikRepository)
   - Get dokter dari jadwal.getDokter()
   - Create Appointment entity (set pasien, dokter, jadwal)
   - Save appointment (AppointmentRepository)
   - Kurangi kuota (JadwalPraktikRepository)
   ↓
4. NotifikasiService.kirimNotifikasi()
   - Create Notifikasi entity (penerima = AppUser pasien)
   - Save notifikasi (NotifikasiRepository)
   ↓
5. Redirect ke /appointment/my-appointments
```

---

## 9. Database Schema Summary

### Tables:
- **users** — Data autentikasi (`AppUser`)
- **pasien** — Data pasien (`Pasien`)
- **dokter** — Data dokter (`Dokter`)
- **admin_rs** — Data admin (`AdminRS`)
- **poli** — Data poli (`Poli`)
- **spesialisasi** — Data spesialisasi (`Spesialisasi`)
- **jadwal_praktik** — Data jadwal praktik (`JadwalPraktik`)
- **appointment** — Data booking appointment (`Appointment`) — memiliki FK ke `pasien`, `dokter`, dan `jadwal_praktik`
- **notifikasi** — Data notifikasi (`Notifikasi`)

### Relationships:
- users (1) ↔ (1) pasien
- users (1) ↔ (1) dokter
- users (1) ↔ (1) admin_rs
- dokter (N) ↔ (1) spesialisasi
- dokter (N) ↔ (1) poli
- dokter (1) ↔ (N) jadwal_praktik
- pasien (1) ↔ (N) appointment
- **dokter (1) ↔ (N) appointment** ← relasi langsung sesuai class diagram
- jadwal_praktik (1) ↔ (N) appointment
- users (1) ↔ (N) notifikasi

---

## 10. Key Features Summary

### Pasien:
- Registrasi akun baru
- Login dan logout
- Dashboard dengan statistik (dari database)
- Cari dokter berdasarkan spesialisasi/nama
- Lihat jadwal dokter (real-time dari database)
- Booking appointment
- Lihat riwayat appointment
- Cancel appointment
- Reschedule appointment
- Update profil

### Dokter:
- Dashboard (data dari database)
- Kelola jadwal praktik (CRUD)
- Lihat daftar pasien
- Lihat pending appointments
- Konfirmasi appointment + kirim notifikasi
- Tolak appointment dengan alasan + kirim notifikasi

### Admin:
- Dashboard dengan statistik harian/bulanan (dari database)
- CRUD dokter (termasuk buat AppUser)
- CRUD poli (dengan validasi referential integrity)
- CRUD spesialisasi (dengan validasi referential integrity)
- Kelola semua jadwal
- Generate dan export laporan (CSV dengan filter bulan/tahun)

---

## 11. Technology Stack

- **Framework:** Spring Boot 3.x
- **Language:** Java 17+
- **Build Tool:** Maven
- **Database:** PostgreSQL/Supabase
- **ORM:** Spring Data JPA (Hibernate)
- **Security:** Spring Security 6
- **Template Engine:** Thymeleaf (wajib `th:text`, `th:each`, `th:if` untuk data dinamis)
- **CSV Export:** Apache Commons CSV
- **Password Encoder:** BCrypt

---

## 12. Ringkasan Perubahan dari Versi Sebelumnya

| # | Bagian | Perubahan | Alasan |
|---|--------|-----------|--------|
| 1 | `Appointment` model | Tambah field `dokter : Dokter` (ManyToOne langsung) | Sesuai class diagram |
| 2 | `AppointmentService.bookAppointment()` | Set field `dokter` dari `jadwal.getDokter()` | Sesuai class diagram |
| 3 | `AppointmentService.rescheduleAppointment()` | Update field `dokter` saat reschedule | Konsistensi dengan point 2 |
| 4 | `AppointmentRepository` | Ganti `findByJadwal_Dokter_IdDokter()` → `findByDokter_IdDokter()` | Efisiensi query, sesuai relasi langsung |
| 5 | `AppointmentRepository` | Tambah `findByBulanDanTahun()` dan `countAllByMonth()` | Dibutuhkan laporan dan export CSV |
| 6 | `DokterService.getDaftarPasien()` | Query via `findByDokter_IdDokter()` | Menggunakan relasi langsung |
| 7 | `DokterService.getPendingAppointments()` | Query via `findByDokter_IdDokterAndStatus()` | Menggunakan relasi langsung |
| 8 | `UserRepository` | Dihapus, digabung ke `AppUserRepository` | Menghilangkan duplikasi |
| 9 | `StatistikDTO` | Dihapus | Lapisan abstraksi tidak perlu |
| 10 | `DashboardStatistik` class/service | Dihapus | Tidak ada di class diagram |
| 11 | `JadwalController` | Dihapus | Tumpang tindih dengan `DokterController` dan `AdminController` |
| 12 | `SpesialisasiService` | Dihapus | Kosong/placeholder, logika cukup di `AdminRSService` |
| 13 | `AdminController` | Tambah endpoint `/admin/kelola-jadwal` langsung | Menggantikan delegasi ke JadwalController |
| 14 | `AdminRSService` | Tambah method statistik individual dan `getAllJadwal()` | Menggantikan DashboardStatistik |
| 15 | `LaporanBulananService.generateCSV()` | Filter berdasarkan bulan/tahun (bukan `findAll()`) | Perbaikan bug export tidak terfilter |
| 16 | `Pasien.updateProfil()` | Hapus parameter `nomorHp` | Tidak ada di class diagram Pasien |
| 17 | Semua template HTML | Wajib menggunakan `th:text`, `th:each`, `th:if` | Data harus berasal dari database |
| 18 | `AdminRSService.deletePoli()` | Tambah validasi dokter terdaftar di poli | Mencegah orphan data |
| 19 | `AdminRSService.deleteSpesialisasi()` | Tambah validasi dokter dengan spesialisasi ini | Mencegah orphan data |
| 20 | `DokterController.dashboard()` | Load data dokter, pending appointment, dan jadwal | Dashboard tidak lagi kosong |
| 21 | `Notifikasi.penerima` | Bertipe relasi ke `AppUser` (implements INotifiable) | Sesuai class diagram |
