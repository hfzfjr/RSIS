-- Tabel Admin RS
CREATE TABLE public.admin_rs (
  id_admin character varying(10) not null,
  id_user character varying(32) not null,
  jabatan character varying(255) null,
  nomor_hp character varying(255) null,
  constraint admin_rs_pkey primary key (id_admin),
  constraint admin_rs_id_user_key unique (id_user),
  constraint admin_rs_id_user_fkey foreign KEY (id_user) references users (id_user) on delete CASCADE deferrable initially DEFERRED
) TABLESPACE pg_default;

-- Tabel Appointment
CREATE TABLE public.appointment (
  id_appointment character varying(10) not null,
  id_pasien character varying(10) not null,
  id_jadwal character varying(10) not null,
  tanggal_booking date not null,
  nomor_antrian character varying(255) null,
  status character varying(255) null default 'MENUNGGU'::character varying,
  catatan character varying(255) null,
  constraint appointment_pkey primary key (id_appointment),
  constraint appointment_id_jadwal_fkey foreign KEY (id_jadwal) references jadwal_praktik (id_jadwal) deferrable initially DEFERRED,
  constraint appointment_id_pasien_fkey foreign KEY (id_pasien) references pasien (id_pasien) on delete CASCADE deferrable initially DEFERRED,
  constraint appointment_status_check check (
    (
      (status)::text = any (
        array[
          ('MENUNGGU'::character varying)::text,
          ('DIKONFIRMASI'::character varying)::text,
          ('DITOLAK'::character varying)::text,
          ('DIBATALKAN'::character varying)::text,
          ('SELESAI'::character varying)::text
        ]
      )
    )
  )
) TABLESPACE pg_default;

-- Tabel Dokter
CREATE TABLE public.dokter (
  id_dokter character varying(10) not null,
  id_user character varying(32) not null,
  nomor_str character varying(255) null,
  id_spesialisasi character varying(10) null,
  id_poli character varying(10) null,
  nomor_hp character varying(255) null,
  constraint dokter_pkey primary key (id_dokter),
  constraint dokter_id_user_key unique (id_user),
  constraint dokter_nomor_str_key unique (nomor_str),
  constraint dokter_id_poli_fkey foreign KEY (id_poli) references poli (id_poli) deferrable initially DEFERRED,
  constraint dokter_id_spesialisasi_fkey foreign KEY (id_spesialisasi) references spesialisasi (id_spesialisasi) deferrable initially DEFERRED,
  constraint dokter_id_user_fkey foreign KEY (id_user) references users (id_user) on delete CASCADE deferrable initially DEFERRED
) TABLESPACE pg_default;

-- Tabel Jadwal Praktik
CREATE TABLE public.jadwal_praktik (
  id_jadwal character varying(10) not null,
  id_dokter character varying(10) not null,
  hari character varying(255) not null,
  tanggal date null,
  jam_mulai time without time zone not null,
  jam_selesai time without time zone not null,
  status_ketersediaan character varying(255) null default 'TERSEDIA'::character varying,
  kuota integer not null default 10,
  sisa_kuota integer not null default 10,
  constraint jadwal_praktik_pkey primary key (id_jadwal),
  constraint jadwal_praktik_id_dokter_fkey foreign KEY (id_dokter) references dokter (id_dokter) on delete CASCADE deferrable initially DEFERRED,
  constraint jadwal_praktik_hari_check check (
    (
      (hari)::text = any (
        array[
          ('SENIN'::character varying)::text,
          ('SELASA'::character varying)::text,
          ('RABU'::character varying)::text,
          ('KAMIS'::character varying)::text,
          ('JUMAT'::character varying)::text,
          ('SABTU'::character varying)::text,
          ('MINGGU'::character varying)::text
        ]
      )
    )
  ),
  constraint jadwal_praktik_status_ketersediaan_check check (
    (
      (status_ketersediaan)::text = any (
        array[
          ('TERSEDIA'::character varying)::text,
          ('PENUH'::character varying)::text,
          ('LIBUR'::character varying)::text
        ]
      )
    )
  )
) TABLESPACE pg_default;

-- Tabel Notifikasi
CREATE TABLE public.notifikasi (
  id_notifikasi character varying(10) not null,
  id_user character varying(32) not null,
  pesan character varying(255) not null,
  tipe character varying(255) null,
  status character varying(255) null default 'BELUM_DIBACA'::character varying,
  tanggal_kirim timestamp without time zone null default CURRENT_TIMESTAMP,
  constraint notifikasi_pkey primary key (id_notifikasi),
  constraint notifikasi_id_user_fkey foreign KEY (id_user) references users (id_user) on delete CASCADE deferrable initially DEFERRED,
  constraint notifikasi_status_check check (
    (
      (status)::text = any (
        array[
          ('BELUM_DIBACA'::character varying)::text,
          ('SUDAH_DIBACA'::character varying)::text
        ]
      )
    )
  ),
  constraint notifikasi_tipe_check check (
    (
      (tipe)::text = any (
        array[
          ('KONFIRMASI'::character varying)::text,
          ('PENOLAKAN'::character varying)::text,
          ('PEMBATALAN'::character varying)::text,
          ('PENGINGAT'::character varying)::text
        ]
      )
    )
  )
) TABLESPACE pg_default;

-- Tabel Pasien
CREATE TABLE public.pasien (
  id_pasien character varying(10) not null,
  id_user character varying(32) not null,
  nomor_rekam_medis character varying(255) null,
  tanggal_lahir date null,
  alamat character varying(255) null,
  nomor_hp character varying(255) null,
  constraint pasien_pkey primary key (id_pasien),
  constraint pasien_id_user_key unique (id_user),
  constraint pasien_nomor_rekam_medis_key unique (nomor_rekam_medis),
  constraint pasien_id_user_fkey foreign KEY (id_user) references users (id_user) on delete CASCADE deferrable initially DEFERRED
) TABLESPACE pg_default;

-- Tabel Poli
CREATE TABLE public.poli (
  id_poli character varying(10) not null,
  nama_poli character varying(255) not null,
  lokasi_ruangan character varying(255) null,
  constraint poli_pkey primary key (id_poli)
) TABLESPACE pg_default;

-- Tabel Spesialisasi
CREATE TABLE public.spesialisasi (
  id_spesialisasi character varying(10) not null,
  nama character varying(255) not null,
  deskripsi character varying(255) null,
  constraint spesialisasi_pkey primary key (id_spesialisasi)
) TABLESPACE pg_default;

-- Data Users
INSERT INTO "public"."users" ("id_user", "nama", "email", "password", "nomor_hp", "role", "created_at") VALUES 
('usr-001', 'Hafiz Fajar Ramadhan', 'hafiz@gmail.com', '11111111', '081111111111', 'PASIEN', '2026-04-28 01:36:28'), 
('usr-002', 'jarr', 'jarr@gmail.com', '$2a$10$FKKgcSJZSehJV7bGz6ZjLO2hb7eHYZ/0kXR/hLfyRFtpNiu6Ve5e.', null, 'PASIEN', '2026-04-29 14:31:10.325632'), 
('usr-003', 'Budi Santoso', 'budi.santoso@rsia.com', 'admin123', '081200000003', 'ADMIN', '2024-01-01 08:00:00'), 
('usr-004', 'Dewi Rahayu', 'dewi.rahayu@rsia.com', '$2b$10$hashedpassword002', '081200000004', 'ADMIN', '2024-01-01 08:00:00'), 
('usr-005', 'dr. Ahmad Fauzi', 'ahmad.fauzi@rsia.com', '11111111', '081200000005', 'DOKTER', '2024-01-02 08:00:00'), 
('usr-006', 'dr. Siti Marlina', 'siti.marlina@rsia.com', '$2b$10$hashedpassword004', '081200000006', 'DOKTER', '2024-01-02 08:00:00'), 
('usr-007', 'dr. Riko Prasetyo', 'riko.prasetyo@rsia.com', '$2b$10$hashedpassword005', '081200000007', 'DOKTER', '2024-01-02 08:00:00'), 
('usr-008', 'dr. Nurul Hidayah', 'nurul.hidayah@rsia.com', '$2b$10$hashedpassword006', '081200000008', 'DOKTER', '2024-01-02 08:00:00'), 
('usr-009', 'dr. Hendra Wijaya', 'hendra.wijaya@rsia.com', '$2b$10$hashedpassword007', '081200000009', 'DOKTER', '2024-01-02 08:00:00'), 
('usr-010', 'dr. Rina Kusuma', 'rina.kusuma@rsia.com', '$2b$10$hashedpassword008', '081200000010', 'DOKTER', '2024-01-02 08:00:00'), 
('usr-011', 'dr. Teguh Santoso', 'teguh.santoso@rsia.com', '$2b$10$hashedpassword009', '081200000011', 'DOKTER', '2024-01-02 08:00:00'), 
('usr-012', 'dr. Lestari Indah', 'lestari.indah@rsia.com', '$2b$10$hashedpassword010', '081200000012', 'DOKTER', '2024-01-02 08:00:00'), 
('usr-013', 'dr. Wahyu Purnomo', 'wahyu.purnomo@rsia.com', '$2b$10$hashedpassword011', '081200000013', 'DOKTER', '2024-01-02 08:00:00'), 
('usr-014', 'dr. Amalia Putri', 'amalia.putri@rsia.com', '$2b$10$hashedpassword012', '081200000014', 'DOKTER', '2024-01-02 08:00:00'), 
('usr-015', 'Agus Setiawan', 'agus.setiawan@gmail.com', '$2b$10$hashedpassword013', '081300000015', 'PASIEN', '2024-01-05 09:00:00'), 
('usr-016', 'Fitri Handayani', 'fitri.handayani@gmail.com', '$2b$10$hashedpassword014', '081300000016', 'PASIEN', '2024-01-06 09:00:00'), 
('usr-017', 'Joko Susilo', 'joko.susilo@gmail.com', '$2b$10$hashedpassword015', '081300000017', 'PASIEN', '2024-01-07 09:00:00'), 
('usr-018', 'Maya Safitri', 'maya.safitri@gmail.com', '$2b$10$hashedpassword016', '081300000018', 'PASIEN', '2024-01-08 09:00:00'), 
('usr-019', 'Rizky Maulana', 'rizky.maulana@gmail.com', '$2b$10$hashedpassword017', '081300000019', 'PASIEN', '2024-01-09 09:00:00'), 
('usr-020', 'Yuni Astuti', 'yuni.astuti@gmail.com', '$2b$10$hashedpassword018', '081300000020', 'PASIEN', '2024-01-10 09:00:00'), 
('usr-021', 'Dimas Pratama', 'dimas.pratama@gmail.com', '$2b$10$hashedpassword019', '081300000021', 'PASIEN', '2024-01-11 09:00:00'), 
('usr-022', 'Ayu Lestari', 'ayu.lestari@gmail.com', '$2b$10$hashedpassword020', '081300000022', 'PASIEN', '2024-01-12 09:00:00'), 
('usr-023', 'Bagas Wicaksono', 'bagas.wicaksono@gmail.com', '$2b$10$hashedpassword021', '081300000023', 'PASIEN', '2024-01-13 09:00:00'), 
('usr-024', 'Citra Dewi', 'citra.dewi@gmail.com', '$2b$10$hashedpassword022', '081300000024', 'PASIEN', '2024-01-14 09:00:00'), 
('usr-025', 'Eko Budiyanto', 'eko.budiyanto@gmail.com', '$2b$10$hashedpassword023', '081300000025', 'PASIEN', '2024-01-15 09:00:00'), 
('usr-026', 'Fajar Nugroho', 'fajar.nugroho@gmail.com', '$2b$10$hashedpassword024', '081300000026', 'PASIEN', '2024-01-16 09:00:00'), 
('usr-027', 'Gita Permata', 'gita.permata@gmail.com', '$2b$10$hashedpassword025', '081300000027', 'PASIEN', '2024-01-17 09:00:00'), 
('usr-028', 'Hadi Pranoto', 'hadi.pranoto@gmail.com', '$2b$10$hashedpassword026', '081300000028', 'PASIEN', '2024-01-18 09:00:00'), 
('usr-029', 'Indra Cahya', 'indra.cahya@gmail.com', '$2b$10$hashedpassword027', '081300000029', 'PASIEN', '2024-01-19 09:00:00'), 
('usr-030', 'Julia Sekar', 'julia.sekar@gmail.com', '$2b$10$hashedpassword028', '081300000030', 'PASIEN', '2024-01-20 09:00:00'), 
('usr-031', 'Kevin Andika', 'kevin.andika@gmail.com', '$2b$10$hashedpassword029', '081300000031', 'PASIEN', '2024-01-21 09:00:00'), 
('usr-032', 'Laras Ningrum', 'laras.ningrum@gmail.com', '$2b$10$hashedpassword030', '081300000032', 'PASIEN', '2024-01-22 09:00:00'), 
('usr-033', 'Muhamad Iqbal', 'muhamad.iqbal@gmail.com', '$2b$10$hashedpassword031', '081300000033', 'PASIEN', '2024-01-23 09:00:00'), 
('usr-034', 'Nadia Rahma', 'nadia.rahma@gmail.com', '$2b$10$hashedpassword032', '081300000034', 'PASIEN', '2024-01-24 09:00:00'), 
('usr-035', 'Oki Firmansyah', 'oki.firmansyah@gmail.com', '$2b$10$hashedpassword033', '081300000035', 'PASIEN', '2024-01-25 09:00:00'), 
('usr-036', 'Prita Wulandari', 'prita.wulandari@gmail.com', '$2b$10$hashedpassword034', '081300000036', 'PASIEN', '2024-01-26 09:00:00'), 
('usr-037', 'Qori Ananda', 'qori.ananda@gmail.com', '$2b$10$hashedpassword035', '081300000037', 'PASIEN', '2024-01-27 09:00:00'), 
('usr-038', 'Reza Alfiansyah', 'reza.alfiansyah@gmail.com', '$2b$10$hashedpassword036', '081300000038', 'PASIEN', '2024-01-28 09:00:00'), 
('usr-039', 'Sari Melati', 'sari.melati@gmail.com', '$2b$10$hashedpassword037', '081300000039', 'PASIEN', '2024-01-29 09:00:00'), 
('usr-040', 'Tono Susanto', 'tono.susanto@gmail.com', '$2b$10$hashedpassword038', '081300000040', 'PASIEN', '2024-01-30 09:00:00'), 
('usr-041', 'Umi Kalsum', 'umi.kalsum@gmail.com', '$2b$10$hashedpassword039', '081300000041', 'PASIEN', '2024-02-01 09:00:00'), 
('usr-042', 'Vino Aditya', 'vino.aditya@gmail.com', '$2b$10$hashedpassword040', '081300000042', 'PASIEN', '2024-02-02 09:00:00'), 
('usr-043', 'Winda Oktavia', 'winda.oktavia@gmail.com', '$2b$10$hashedpassword041', '081300000043', 'PASIEN', '2024-02-03 09:00:00'), 
('usr-044', 'Xena Lorenza', 'xena.lorenza@gmail.com', '$2b$10$hashedpassword042', '081300000044', 'PASIEN', '2024-02-04 09:00:00'), 
('usr-045', 'Yoga Pradipta', 'yoga.pradipta@gmail.com', '$2b$10$hashedpassword043', '081300000045', 'PASIEN', '2024-02-05 09:00:00'), 
('usr-046', 'Zahra Aulia', 'zahra.aulia@gmail.com', '$2b$10$hashedpassword044', '081300000046', 'PASIEN', '2024-02-06 09:00:00'), 
('usr-047', 'Anton Wijaya', 'anton.wijaya@gmail.com', '$2b$10$hashedpassword045', '081300000047', 'PASIEN', '2024-02-07 09:00:00'), 
('usr-048', 'Bella Nuraini', 'bella.nuraini@gmail.com', '$2b$10$hashedpassword046', '081300000048', 'PASIEN', '2024-02-08 09:00:00'), 
('usr-049', 'Cahyo Nugroho', 'cahyo.nugroho@gmail.com', '$2b$10$hashedpassword047', '081300000049', 'PASIEN', '2024-02-09 09:00:00'), 
('usr-050', 'Diana Puspita', 'diana.puspita@gmail.com', '$2b$10$hashedpassword048', '081300000050', 'PASIEN', '2024-02-10 09:00:00'), 
('usr-051', 'Erwin Saputra', 'erwin.saputra@gmail.com', '$2b$10$hashedpassword049', '081300000051', 'PASIEN', '2024-02-11 09:00:00'), 
('usr-052', 'Fanny Kusuma', 'fanny.kusuma@gmail.com', '$2b$10$hashedpassword050', '081300000052', 'PASIEN', '2024-02-12 09:00:00'), 
('usr-053', 'ramadhan', 'ramadhan@gmail.com', '$2a$10$UNjzZBNC1QnDjP4vZeVLtu26BjvEqSih3g6UaHygzGIizA6yq6LDO', null, 'PASIEN', '2026-05-09 07:42:03.215319');

-- Data Pasien
INSERT INTO "public"."pasien" ("id_pasien", "id_user", "nomor_rekam_medis", "tanggal_lahir", "alamat", "nomor_hp") VALUES 
('psn-001', 'usr-001', 'RM-2024-0001', '1990-05-15', 'Jl. Melati No. 1, Jakarta Selatan', null), 
('psn-002', 'usr-002', 'RM-2024-0002', '1988-03-22', 'Jl. Mawar No. 2, Jakarta Timur', null), 
('psn-003', 'usr-015', 'RM-2024-0003', '1992-07-10', 'Jl. Anggrek No. 3, Depok', null), 
('psn-004', 'usr-016', 'RM-2024-0004', '1985-11-30', 'Jl. Dahlia No. 4, Bekasi', null), 
('psn-005', 'usr-017', 'RM-2024-0005', '1995-02-18', 'Jl. Kenanga No. 5, Bogor', null), 
('psn-006', 'usr-018', 'RM-2024-0006', '1993-08-25', 'Jl. Flamboyan No. 6, Tangerang', null), 
('psn-007', 'usr-019', 'RM-2024-0007', '1998-04-12', 'Jl. Cempaka No. 7, Jakarta Barat', null), 
('psn-008', 'usr-020', 'RM-2024-0008', '1980-09-05', 'Jl. Teratai No. 8, Jakarta Utara', null), 
('psn-009', 'usr-021', 'RM-2024-0009', '2000-01-20', 'Jl. Tulip No. 9, Jakarta Pusat', null), 
('psn-010', 'usr-022', 'RM-2024-0010', '1975-06-14', 'Jl. Seruni No. 10, Depok', null), 
('psn-011', 'usr-023', 'RM-2024-0011', '1991-12-03', 'Jl. Kamboja No. 11, Bekasi', null), 
('psn-012', 'usr-024', 'RM-2024-0012', '1987-03-17', 'Jl. Wisteria No. 12, Bogor', null), 
('psn-013', 'usr-025', 'RM-2024-0013', '1994-10-28', 'Jl. Lavender No. 13, Tangerang', null), 
('psn-014', 'usr-026', 'RM-2024-0014', '1996-07-07', 'Jl. Bougenville No. 14, Jakarta Selatan', null), 
('psn-015', 'usr-027', 'RM-2024-0015', '1983-05-21', 'Jl. Sakura No. 15, Jakarta Timur', null), 
('psn-016', 'usr-028', 'RM-2024-0016', '1979-02-09', 'Jl. Gladiol No. 16, Jakarta Barat', null), 
('psn-017', 'usr-029', 'RM-2024-0017', '2001-08-16', 'Jl. Aster No. 17, Jakarta Pusat', null), 
('psn-018', 'usr-030', 'RM-2024-0018', '1997-11-11', 'Jl. Krisan No. 18, Depok', null), 
('psn-019', 'usr-031', 'RM-2024-0019', '1990-04-04', 'Jl. Amarilis No. 19, Bekasi', null), 
('psn-020', 'usr-032', 'RM-2024-0020', '1986-09-30', 'Jl. Petunia No. 20, Bogor', null), 
('psn-021', 'usr-033', 'RM-2024-0021', '1993-01-15', 'Jl. Azalea No. 21, Tangerang', null), 
('psn-022', 'usr-034', 'RM-2024-0022', '1999-06-23', 'Jl. Begonia No. 22, Jakarta Selatan', null), 
('psn-023', 'usr-035', 'RM-2024-0023', '1977-03-08', 'Jl. Dahlia No. 23, Jakarta Timur', null), 
('psn-024', 'usr-036', 'RM-2024-0024', '1995-12-19', 'Jl. Erica No. 24, Jakarta Barat', null), 
('psn-025', 'usr-037', 'RM-2024-0025', '2002-07-26', 'Jl. Forsythia No. 25, Jakarta Utara', null), 
('psn-026', 'usr-038', 'RM-2024-0026', '1988-10-01', 'Jl. Gardenia No. 26, Jakarta Pusat', null), 
('psn-027', 'usr-039', 'RM-2024-0027', '1984-05-14', 'Jl. Heather No. 27, Depok', null), 
('psn-028', 'usr-040', 'RM-2024-0028', '1992-02-28', 'Jl. Iris No. 28, Bekasi', null), 
('psn-029', 'usr-041', 'RM-2024-0029', '1981-08-17', 'Jl. Jasmine No. 29, Bogor', null), 
('psn-030', 'usr-042', 'RM-2024-0030', '1998-11-05', 'Jl. Kumquat No. 30, Tangerang', null), 
('psn-031', 'usr-043', 'RM-2024-0031', '1996-04-22', 'Jl. Lili No. 31, Jakarta Selatan', null), 
('psn-032', 'usr-044', 'RM-2024-0032', '1973-09-13', 'Jl. Mimosa No. 32, Jakarta Timur', null), 
('psn-033', 'usr-045', 'RM-2024-0033', '2003-01-09', 'Jl. Narcissus No. 33, Jakarta Barat', null), 
('psn-034', 'usr-046', 'RM-2024-0034', '1989-06-18', 'Jl. Oleander No. 34, Jakarta Pusat', null), 
('psn-035', 'usr-047', 'RM-2024-0035', '1976-03-27', 'Jl. Poppy No. 35, Depok', null), 
('psn-036', 'usr-048', 'RM-2024-0036', '1994-10-06', 'Jl. Quince No. 36, Bekasi', null), 
('psn-037', 'usr-049', 'RM-2024-0037', '1991-07-31', 'Jl. Ranunculus No. 37, Bogor', null), 
('psn-038', 'usr-050', 'RM-2024-0038', '1987-12-24', 'Jl. Sunflower No. 38, Tangerang', null), 
('psn-039', 'usr-051', 'RM-2024-0039', '2000-05-02', 'Jl. Thistle No. 39, Jakarta Selatan', null), 
('psn-040', 'usr-052', 'RM-2024-0040', '1983-02-15', 'Jl. Ursinia No. 40, Jakarta Timur', null), 
('psn-041', 'usr-053', null, null, null, null);

-- Data Spesialisasi
INSERT INTO "public"."spesialisasi" ("id_spesialisasi", "nama", "deskripsi") VALUES 
('sps-001', 'Penyakit Dalam', 'Menangani penyakit organ dalam seperti diabetes, hipertensi, dan gangguan metabolisme.'), 
('sps-002', 'Bedah Umum', 'Menangani tindakan operasi umum termasuk appendisitis dan hernia.'), 
('sps-003', 'Anak', 'Menangani kesehatan bayi, anak, dan remaja.'), 
('sps-004', 'Kandungan & Kebidanan', 'Menangani kesehatan reproduksi wanita, kehamilan, dan persalinan.'), 
('sps-005', 'Jantung & Pembuluh Darah', 'Menangani penyakit jantung koroner, aritmia, dan gagal jantung.'), 
('sps-006', 'Saraf', 'Menangani penyakit stroke, epilepsi, dan gangguan saraf tepi.'), 
('sps-007', 'Kulit & Kelamin', 'Menangani penyakit kulit, alergi dermatologi, dan kelamin.'), 
('sps-008', 'Mata', 'Menangani gangguan penglihatan, katarak, dan penyakit retina.'), 
('sps-009', 'THT', 'Menangani gangguan telinga, hidung, dan tenggorokan.'), 
('sps-010', 'Ortopedi', 'Menangani penyakit tulang, sendi, dan otot.');

-- Data Poli
INSERT INTO "public"."poli" ("id_poli", "nama_poli", "lokasi_ruangan") VALUES 
('pli-001', 'Poli Penyakit Dalam', 'Gedung A, Lantai 1, Ruang 101'), 
('pli-002', 'Poli Bedah', 'Gedung A, Lantai 2, Ruang 201'), 
('pli-003', 'Poli Anak', 'Gedung B, Lantai 1, Ruang 101'), 
('pli-004', 'Poli Kandungan & Kebidanan', 'Gedung B, Lantai 1, Ruang 102'), 
('pli-005', 'Poli Jantung', 'Gedung A, Lantai 2, Ruang 202'), 
('pli-006', 'Poli Saraf', 'Gedung A, Lantai 3, Ruang 301'), 
('pli-007', 'Poli Kulit & Kelamin', 'Gedung B, Lantai 2, Ruang 201'), 
('pli-008', 'Poli Mata', 'Gedung B, Lantai 2, Ruang 202'), 
('pli-009', 'Poli THT', 'Gedung A, Lantai 1, Ruang 102'), 
('pli-010', 'Poli Ortopedi', 'Gedung C, Lantai 1, Ruang 101');

-- Data Dokter
INSERT INTO "public"."dokter" ("id_dokter", "id_user", "nomor_str", "id_spesialisasi", "id_poli", "nomor_hp") VALUES 
('dkt-001', 'usr-005', 'STR-2024-0001', 'sps-001', 'pli-001', null), 
('dkt-002', 'usr-006', 'STR-2024-0002', 'sps-003', 'pli-003', null), 
('dkt-003', 'usr-007', 'STR-2024-0003', 'sps-004', 'pli-004', null), 
('dkt-004', 'usr-008', 'STR-2024-0004', 'sps-005', 'pli-005', null), 
('dkt-005', 'usr-009', 'STR-2024-0005', 'sps-002', 'pli-002', null), 
('dkt-006', 'usr-010', 'STR-2024-0006', 'sps-006', 'pli-006', null), 
('dkt-007', 'usr-011', 'STR-2024-0007', 'sps-007', 'pli-007', null), 
('dkt-008', 'usr-012', 'STR-2024-0008', 'sps-008', 'pli-008', null), 
('dkt-009', 'usr-013', 'STR-2024-0009', 'sps-009', 'pli-009', null), 
('dkt-010', 'usr-014', 'STR-2024-0010', 'sps-010', 'pli-010', null);

-- Data Admin RS
INSERT INTO "public"."admin_rs" ("id_admin", "id_user", "jabatan", "nomor_hp") VALUES 
('adm-001', 'usr-003', 'Kepala Administrasi', null), 
('adm-002', 'usr-004', 'Staff Administrasi', null);

-- Data Jadwal Praktik
INSERT INTO "public"."jadwal_praktik" ("id_jadwal", "id_dokter", "hari", "tanggal", "jam_mulai", "jam_selesai", "status_ketersediaan", "kuota", "sisa_kuota") VALUES 
('jdw-001', 'dkt-001', 'SENIN', null, '08:00:00', '12:00:00', 'TERSEDIA', 15, 10), 
('jdw-002', 'dkt-001', 'KAMIS', null, '13:00:00', '17:00:00', 'TERSEDIA', 15, 5), 
('jdw-003', 'dkt-002', 'SELASA', null, '08:00:00', '12:00:00', 'TERSEDIA', 12, 12), 
('jdw-004', 'dkt-002', 'JUMAT', null, '13:00:00', '16:00:00', 'PENUH', 12, 0), 
('jdw-005', 'dkt-003', 'RABU', null, '08:00:00', '12:00:00', 'TERSEDIA', 10, 8), 
('jdw-006', 'dkt-003', 'SABTU', null, '08:00:00', '11:00:00', 'TERSEDIA', 10, 3), 
('jdw-007', 'dkt-004', 'SENIN', null, '13:00:00', '17:00:00', 'TERSEDIA', 10, 7), 
('jdw-008', 'dkt-004', 'RABU', null, '08:00:00', '12:00:00', 'PENUH', 10, 0), 
('jdw-009', 'dkt-005', 'SELASA', null, '08:00:00', '13:00:00', 'TERSEDIA', 15, 9), 
('jdw-010', 'dkt-005', 'KAMIS', null, '13:00:00', '17:00:00', 'TERSEDIA', 15, 15), 
('jdw-011', 'dkt-006', 'RABU', null, '08:00:00', '12:00:00', 'TERSEDIA', 10, 6), 
('jdw-012', 'dkt-006', 'JUMAT', null, '08:00:00', '12:00:00', 'TERSEDIA', 10, 10), 
('jdw-013', 'dkt-007', 'SENIN', null, '08:00:00', '11:00:00', 'TERSEDIA', 12, 4), 
('jdw-014', 'dkt-007', 'KAMIS', null, '13:00:00', '16:00:00', 'PENUH', 12, 0), 
('jdw-015', 'dkt-008', 'SELASA', null, '13:00:00', '17:00:00', 'TERSEDIA', 10, 8), 
('jdw-016', 'dkt-008', 'SABTU', null, '08:00:00', '11:00:00', 'TERSEDIA', 10, 2), 
('jdw-017', 'dkt-009', 'RABU', null, '13:00:00', '17:00:00', 'TERSEDIA', 15, 11), 
('jdw-018', 'dkt-009', 'JUMAT', null, '08:00:00', '13:00:00', 'LIBUR', 15, 0), 
('jdw-019', 'dkt-010', 'KAMIS', null, '08:00:00', '12:00:00', 'TERSEDIA', 12, 7), 
('jdw-020', 'dkt-010', 'SABTU', null, '08:00:00', '12:00:00', 'TERSEDIA', 12, 5);

-- Data Appointment
INSERT INTO "public"."appointment" ("id_appointment", "id_pasien", "id_jadwal", "tanggal_booking", "nomor_antrian", "status", "catatan") VALUES 
('apt-001', 'psn-001', 'jdw-001', '2024-04-01', 'A001', 'SELESAI', 'Kontrol rutin diabetes'), 
('apt-002', 'psn-002', 'jdw-003', '2024-04-01', 'B001', 'SELESAI', 'Pemeriksaan tumbuh kembang anak'), 
('apt-003', 'psn-003', 'jdw-007', '2024-04-01', 'C001', 'SELESAI', 'EKG dan konsultasi jantung'), 
('apt-004', 'psn-004', 'jdw-005', '2024-04-02', 'A001', 'SELESAI', 'Kontrol kehamilan trimester 2'), 
('apt-005', 'psn-005', 'jdw-009', '2024-04-02', 'B001', 'SELESAI', 'Konsultasi post operasi hernia'), 
('apt-006', 'psn-006', 'jdw-011', '2024-04-03', 'A001', 'SELESAI', 'Kontrol migrain kronis'), 
('apt-007', 'psn-007', 'jdw-013', '2024-04-03', 'B001', 'SELESAI', 'Pengobatan eksim'), 
('apt-008', 'psn-008', 'jdw-015', '2024-04-04', 'A001', 'SELESAI', 'Pemeriksaan katarak'), 
('apt-009', 'psn-009', 'jdw-017', '2024-04-04', 'B001', 'SELESAI', 'Penanganan sinusitis'), 
('apt-010', 'psn-010', 'jdw-019', '2024-04-04', 'A001', 'SELESAI', 'Kontrol nyeri lutut'), 
('apt-011', 'psn-011', 'jdw-002', '2024-04-08', 'A001', 'SELESAI', 'Konsultasi gula darah tinggi'), 
('apt-012', 'psn-012', 'jdw-004', '2024-04-09', 'A001', 'DIBATALKAN', 'Pasien batal hadir'), 
('apt-013', 'psn-013', 'jdw-006', '2024-04-13', 'A001', 'SELESAI', 'Kontrol kehamilan trimester 3'), 
('apt-014', 'psn-014', 'jdw-008', '2024-04-10', 'A001', 'DITOLAK', 'Jadwal penuh, dialihkan ke jadwal lain'), 
('apt-015', 'psn-015', 'jdw-010', '2024-04-11', 'A001', 'SELESAI', 'Konsultasi pra operasi bedah jantung'), 
('apt-016', 'psn-016', 'jdw-012', '2024-04-12', 'A001', 'SELESAI', 'Pemeriksaan saraf kejepit'), 
('apt-017', 'psn-017', 'jdw-014', '2024-04-11', 'A001', 'DITOLAK', 'Jadwal penuh'), 
('apt-018', 'psn-018', 'jdw-016', '2024-04-13', 'A001', 'SELESAI', 'Pemeriksaan minus mata tinggi'), 
('apt-019', 'psn-019', 'jdw-020', '2024-04-13', 'A001', 'SELESAI', 'Pengobatan fraktur tangan'), 
('apt-020', 'psn-020', 'jdw-001', '2024-04-15', 'A002', 'SELESAI', 'Kontrol kolesterol'), 
('apt-021', 'psn-021', 'jdw-003', '2024-04-16', 'B002', 'SELESAI', 'Imunisasi anak'), 
('apt-022', 'psn-022', 'jdw-005', '2024-04-17', 'A002', 'SELESAI', 'USG kandungan'), 
('apt-023', 'psn-023', 'jdw-007', '2024-04-15', 'C002', 'SELESAI', 'Ekokardiografi'), 
('apt-024', 'psn-024', 'jdw-009', '2024-04-16', 'B002', 'SELESAI', 'Perawatan luka pasca bedah'), 
('apt-025', 'psn-025', 'jdw-011', '2024-04-17', 'A002', 'SELESAI', 'Konsultasi vertigo'), 
('apt-026', 'psn-026', 'jdw-013', '2024-04-15', 'B002', 'SELESAI', 'Perawatan jerawat inflamasi'), 
('apt-027', 'psn-027', 'jdw-015', '2024-04-16', 'A002', 'SELESAI', 'Kontrol glaukoma'), 
('apt-028', 'psn-028', 'jdw-017', '2024-04-17', 'B002', 'SELESAI', 'Operasi amandel'), 
('apt-029', 'psn-029', 'jdw-019', '2024-04-18', 'A002', 'SELESAI', 'Fisioterapi lutut'), 
('apt-030', 'psn-030', 'jdw-002', '2024-04-18', 'A002', 'SELESAI', 'Kontrol asam urat'), 
('apt-031', 'psn-031', 'jdw-004', '2024-04-19', 'A002', 'DIBATALKAN', 'Pasien tidak bisa hadir'), 
('apt-032', 'psn-032', 'jdw-006', '2024-04-20', 'A002', 'SELESAI', 'ANC trimester 1'), 
('apt-033', 'psn-033', 'jdw-008', '2024-04-19', 'A002', 'DITOLAK', 'Jadwal penuh'), 
('apt-034', 'psn-034', 'jdw-010', '2024-04-18', 'A002', 'SELESAI', 'Konsultasi pasca bypass jantung'), 
('apt-035', 'psn-035', 'jdw-012', '2024-04-19', 'A002', 'SELESAI', 'MRI saraf tulang belakang'), 
('apt-036', 'psn-036', 'jdw-014', '2024-04-18', 'A002', 'DITOLAK', 'Jadwal penuh'), 
('apt-037', 'psn-037', 'jdw-016', '2024-04-20', 'A002', 'SELESAI', 'Operasi katarak ringan'), 
('apt-038', 'psn-038', 'jdw-020', '2024-04-20', 'A002', 'SELESAI', 'Pasang gips fraktur kaki'), 
('apt-039', 'psn-039', 'jdw-001', '2024-04-22', 'A003', 'DIKONFIRMASI', 'Konsultasi anemia'), 
('apt-040', 'psn-040', 'jdw-003', '2024-04-23', 'B003', 'DIKONFIRMASI', 'Pemeriksaan diare anak'), 
('apt-041', 'psn-001', 'jdw-007', '2024-04-22', 'C003', 'MENUNGGU', 'Kontrol tekanan darah'), 
('apt-042', 'psn-002', 'jdw-005', '2024-04-23', 'A003', 'MENUNGGU', 'Kontrol kehamilan'), 
('apt-043', 'psn-003', 'jdw-009', '2024-04-23', 'B003', 'DIKONFIRMASI', 'Konsultasi appendisitis'), 
('apt-044', 'psn-004', 'jdw-011', '2024-04-24', 'A003', 'MENUNGGU', 'Keluhan pusing berkepanjangan'), 
('apt-045', 'psn-005', 'jdw-013', '2024-04-24', 'B003', 'DIKONFIRMASI', 'Cek kulit ruam alergi'), 
('apt-046', 'psn-006', 'jdw-015', '2024-04-24', 'A003', 'MENUNGGU', 'Kontrol minus mata'), 
('apt-047', 'psn-007', 'jdw-017', '2024-04-25', 'B003', 'DIKONFIRMASI', 'Keluhan pendengaran berkurang'), 
('apt-048', 'psn-008', 'jdw-019', '2024-04-25', 'A003', 'MENUNGGU', 'Nyeri sendi lutut kiri'), 
('apt-049', 'psn-009', 'jdw-002', '2024-04-25', 'A003', 'DIKONFIRMASI', 'Kontrol hipertensi'), 
('apt-050', 'psn-010', 'jdw-020', '2024-04-26', 'A003', 'MENUNGGU', 'Rehabilitasi cedera bahu');

-- Data Notifikasi
INSERT INTO "public"."notifikasi" ("id_notifikasi", "id_user", "pesan", "tipe", "status", "tanggal_kirim") VALUES 
('ntf-001', 'usr-001', 'Appointment Anda pada Senin, 01 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-03-30 10:00:00'), 
('ntf-002', 'usr-002', 'Appointment Anda pada Selasa, 02 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-03-31 10:00:00'), 
('ntf-003', 'usr-015', 'Appointment Anda pada Senin, 01 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-03-30 10:05:00'), 
('ntf-004', 'usr-016', 'Appointment Anda pada Rabu, 03 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-01 10:00:00'), 
('ntf-005', 'usr-017', 'Appointment Anda pada Selasa, 02 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-01 10:10:00'), 
('ntf-006', 'usr-018', 'Appointment Anda pada Rabu, 03 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-02 09:00:00'), 
('ntf-007', 'usr-019', 'Appointment Anda pada Rabu, 03 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-02 09:05:00'), 
('ntf-008', 'usr-020', 'Appointment Anda pada Kamis, 04 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-03 09:00:00'), 
('ntf-009', 'usr-021', 'Appointment Anda pada Kamis, 04 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-03 09:10:00'), 
('ntf-010', 'usr-022', 'Appointment Anda pada Kamis, 04 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-03 09:20:00'), 
('ntf-011', 'usr-023', 'Appointment Anda pada Senin, 08 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-06 10:00:00'), 
('ntf-012', 'usr-024', 'Appointment Anda pada Selasa, 09 April 2024 telah dibatalkan.', 'PEMBATALAN', 'SUDAH_DIBACA', '2024-04-09 08:00:00'), 
('ntf-013', 'usr-025', 'Appointment Anda pada Sabtu, 13 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-11 10:00:00'), 
('ntf-014', 'usr-026', 'Mohon maaf, appointment Anda ditolak karena jadwal penuh.', 'PENOLAKAN', 'SUDAH_DIBACA', '2024-04-10 08:00:00'), 
('ntf-015', 'usr-027', 'Appointment Anda pada Kamis, 11 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-09 10:00:00'), 
('ntf-016', 'usr-028', 'Appointment Anda pada Jumat, 12 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-10 10:00:00'), 
('ntf-017', 'usr-029', 'Mohon maaf, appointment Anda ditolak karena jadwal penuh.', 'PENOLAKAN', 'SUDAH_DIBACA', '2024-04-11 08:00:00'), 
('ntf-018', 'usr-030', 'Appointment Anda pada Sabtu, 13 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-11 10:10:00'), 
('ntf-019', 'usr-031', 'Appointment Anda pada Sabtu, 13 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-11 10:15:00'), 
('ntf-020', 'usr-032', 'Appointment Anda pada Senin, 15 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-13 10:00:00'), 
('ntf-021', 'usr-033', 'Appointment Anda pada Senin, 15 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-13 10:05:00'), 
('ntf-022', 'usr-034', 'Appointment Anda pada Rabu, 17 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-15 10:00:00'), 
('ntf-023', 'usr-035', 'Appointment Anda pada Senin, 15 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-13 10:10:00'), 
('ntf-024', 'usr-036', 'Appointment Anda pada Selasa, 16 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-14 10:00:00'), 
('ntf-025', 'usr-037', 'Appointment Anda pada Rabu, 17 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-15 10:05:00'), 
('ntf-026', 'usr-038', 'Appointment Anda pada Senin, 15 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-13 10:15:00'), 
('ntf-027', 'usr-039', 'Appointment Anda pada Selasa, 16 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-14 10:05:00'), 
('ntf-028', 'usr-040', 'Appointment Anda pada Rabu, 17 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-15 10:10:00'), 
('ntf-029', 'usr-041', 'Appointment Anda pada Kamis, 18 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-16 10:00:00'), 
('ntf-030', 'usr-042', 'Appointment Anda pada Kamis, 18 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-16 10:05:00'), 
('ntf-031', 'usr-043', 'Mohon maaf, appointment Anda ditolak karena jadwal penuh.', 'PENOLAKAN', 'SUDAH_DIBACA', '2024-04-19 08:00:00'), 
('ntf-032', 'usr-044', 'Appointment Anda pada Jumat, 19 April 2024 telah dibatalkan.', 'PEMBATALAN', 'SUDAH_DIBACA', '2024-04-19 08:05:00'), 
('ntf-033', 'usr-045', 'Appointment Anda pada Sabtu, 20 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-18 10:00:00'), 
('ntf-034', 'usr-046', 'Mohon maaf, appointment Anda ditolak karena jadwal penuh.', 'PENOLAKAN', 'SUDAH_DIBACA', '2024-04-18 08:00:00'), 
('ntf-035', 'usr-047', 'Appointment Anda pada Kamis, 18 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-16 10:10:00'), 
('ntf-036', 'usr-048', 'Appointment Anda pada Sabtu, 20 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-18 10:05:00'), 
('ntf-037', 'usr-049', 'Appointment Anda pada Sabtu, 20 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-18 10:10:00'), 
('ntf-038', 'usr-050', 'Appointment Anda pada Sabtu, 20 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-18 10:15:00'), 
('ntf-039', 'usr-051', 'Pengingat: Appointment Anda besok Senin, 22 April 2024 pukul 08.00.', 'PENGINGAT', 'SUDAH_DIBACA', '2024-04-21 18:00:00'), 
('ntf-040', 'usr-052', 'Pengingat: Appointment Anda besok Selasa, 23 April 2024 pukul 08.00.', 'PENGINGAT', 'SUDAH_DIBACA', '2024-04-22 18:00:00'), 
('ntf-041', 'usr-001', 'Pengingat: Appointment Anda besok Senin, 22 April 2024 pukul 13.00.', 'PENGINGAT', 'SUDAH_DIBACA', '2024-04-21 18:05:00'), 
('ntf-042', 'usr-002', 'Pengingat: Appointment Anda besok Selasa, 23 April 2024 pukul 08.00.', 'PENGINGAT', 'SUDAH_DIBACA', '2024-04-22 18:05:00'), 
('ntf-043', 'usr-015', 'Appointment Anda pada Selasa, 23 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'SUDAH_DIBACA', '2024-04-21 10:00:00'), 
('ntf-044', 'usr-016', 'Pengingat: Appointment Anda besok Rabu, 24 April 2024 pukul 08.00.', 'PENGINGAT', 'BELUM_DIBACA', '2024-04-23 18:00:00'), 
('ntf-045', 'usr-017', 'Appointment Anda pada Rabu, 24 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'BELUM_DIBACA', '2024-04-22 10:00:00'), 
('ntf-046', 'usr-018', 'Pengingat: Appointment Anda besok Kamis, 25 April 2024 pukul 08.00.', 'PENGINGAT', 'BELUM_DIBACA', '2024-04-24 18:00:00'), 
('ntf-047', 'usr-019', 'Appointment Anda pada Kamis, 25 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'BELUM_DIBACA', '2024-04-23 10:00:00'), 
('ntf-048', 'usr-020', 'Pengingat: Appointment Anda besok Jumat, 26 April 2024 pukul 13.00.', 'PENGINGAT', 'BELUM_DIBACA', '2024-04-25 18:00:00'), 
('ntf-049', 'usr-021', 'Appointment Anda pada Kamis, 25 April 2024 telah dikonfirmasi.', 'KONFIRMASI', 'BELUM_DIBACA', '2024-04-23 10:05:00'), 
('ntf-050', 'usr-022', 'Pengingat: Appointment Anda besok Jumat, 26 April 2024 pukul 08.00.', 'PENGINGAT', 'BELUM_DIBACA', '2024-04-25 18:05:00');