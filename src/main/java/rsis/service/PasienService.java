package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
import rsis.repository.UserRepository;
import rsis.repository.DokterRepository;
import rsis.repository.JadwalPraktikRepository;
import rsis.repository.PasienRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PasienService {

    @Autowired
    private PasienRepository pasienRepository;

    @Autowired
    private DokterRepository dokterRepository;

    @Autowired
    private JadwalPraktikRepository jadwalPraktikRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Dokter> cariDokter(String keyword) {
        List<Dokter> dokters;
        if (keyword == null || keyword.isEmpty()) {
            dokters = dokterRepository.findAll();
        } else {
            dokters = dokterRepository.searchBySpesialisasiOrNama(keyword);
        }
        // Populate transient fields from users table
        return populateDokterWithUserData(dokters);
    }

    private List<Dokter> populateDokterWithUserData(List<Dokter> dokters) {
        return dokters.stream().map(dokter -> {
            userRepository.findById(dokter.getIdUser()).ifPresent(user -> {
                dokter.setNama(user.getNama());
                dokter.setEmail(user.getEmail());
                dokter.setPassword(user.getPassword());
                dokter.setRole(user.getRole());
            });
            return dokter;
        }).toList();
    }

    public List<Dokter> cariDokterBySpesialisasi(String spesialisasi) {
        return dokterRepository.findBySpesialisasi_Nama(spesialisasi);
    }

    public List<JadwalPraktik> lihatJadwalDokter(String dokterId) {
        return jadwalPraktikRepository.findAvailableJadwalByDokterId(dokterId);
    }

    @Transactional
    public Pasien updateProfil(String pasienId, String namaLengkap, String nomorRekamMedis, String tanggalLahir,
            String alamat) {
        Optional<Pasien> pasienOpt = pasienRepository.findById(pasienId);
        if (pasienOpt.isEmpty()) {
            throw new RuntimeException("Pasien not found");
        }

        Pasien pasien = pasienOpt.get();
        if (namaLengkap != null) {
            pasien.setNama(namaLengkap);
        }
        if (nomorRekamMedis != null) {
            pasien.setNomorRekamMedis(nomorRekamMedis);
        }
        if (tanggalLahir != null) {
            pasien.setTanggalLahir(java.time.LocalDate.parse(tanggalLahir));
        }
        if (alamat != null) {
            pasien.setAlamat(alamat);
        }

        return pasienRepository.save(pasien);
    }

    public Optional<Pasien> getPasienById(String pasienId) {
        return pasienRepository.findById(pasienId);
    }

    public Optional<Pasien> getPasienByEmail(String email) {
        // Find user by email first, then find pasien by user id
        Optional<rsis.model.User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isPresent()) {
            return pasienRepository.findByIdUser(userOpt.get().getIdUser());
        }
        return Optional.empty();
    }

    @Transactional
    public Pasien createPasien(Pasien pasien) {
        String idPasien = generatePasienId();
        pasien.setIdPasien(idPasien);
        pasien.setRole("PASIEN");
        return pasienRepository.save(pasien);
    }

    private String generatePasienId() {
        Optional<String> latestId = pasienRepository.findLatestPasienId();
        if (latestId.isPresent()) {
            String id = latestId.get();
            int num = Integer.parseInt(id.substring(4));
            return String.format("psn-%04d", num + 1);
        }
        return "psn-0001";
    }
}
