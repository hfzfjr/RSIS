package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
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

    public List<Dokter> cariDokter(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return dokterRepository.findAll();
        }
        return dokterRepository.searchBySpesialisasiOrNama(keyword);
    }

    public List<Dokter> cariDokterBySpesialisasi(String spesialisasi) {
        return dokterRepository.findBySpesialisasi_Nama(spesialisasi);
    }

    public List<JadwalPraktik> lihatJadwalDokter(String dokterId) {
        return jadwalPraktikRepository.findAvailableJadwalByDokterId(dokterId);
    }

    @Transactional
    public Pasien updateProfil(String pasienId, String nomorRekamMedis, String tanggalLahir, String alamat,
            String nomorHp) {
        Optional<Pasien> pasienOpt = pasienRepository.findById(pasienId);
        if (pasienOpt.isEmpty()) {
            throw new RuntimeException("Pasien not found");
        }

        Pasien pasien = pasienOpt.get();
        if (nomorRekamMedis != null) {
            pasien.setNomorRekamMedis(nomorRekamMedis);
        }
        if (tanggalLahir != null) {
            pasien.setTanggalLahir(java.time.LocalDate.parse(tanggalLahir));
        }
        if (alamat != null) {
            pasien.setAlamat(alamat);
        }
        if (nomorHp != null) {
            pasien.setNomorHp(nomorHp);
        }

        return pasienRepository.save(pasien);
    }

    public Optional<Pasien> getPasienById(String pasienId) {
        return pasienRepository.findById(pasienId);
    }

    public Optional<Pasien> getPasienByEmail(String email) {
        return pasienRepository.findByEmail(email);
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
