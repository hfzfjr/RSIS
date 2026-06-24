package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.Dokter;
import rsis.model.Poli;
import rsis.repository.DokterRepository;
import rsis.repository.PoliRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PoliService {

    @Autowired
    private PoliRepository poliRepository;

    @Autowired
    private DokterRepository dokterRepository;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Autowired
    private DokterService dokterService;

    // ====================
    // Poli Management
    // ====================

    @Transactional
    public void createPoli(Poli poli, List<String> dokterIds) {
        String idPoli = idGeneratorService.generatePoliId(poliRepository.count());
        poli.setIdPoli(idPoli);
        poli.setIsActive(true);
        Poli savedPoli = poliRepository.save(poli);

        // Associate new doctors to this poli
        if (dokterIds != null) {
            for (String docId : dokterIds) {
                if (docId != null) {
                    dokterRepository.findById(docId).ifPresent(d -> {
                        d.setPoli(savedPoli);
                        dokterRepository.save(d);
                    });
                }
            }
        }
    }

    @Transactional
    public Poli updatePoli(Poli poli) {
        if (poli == null) {
            throw new RuntimeException("Poli cannot be null");
        }
        return poliRepository.save(poli);
    }

    public List<Dokter> getDokterByPoli(String idPoli) {
        List<Dokter> dokters = dokterRepository.findByPoli_IdPoli(idPoli);
        return dokters.stream()
                .filter(d -> d.getIsActive() != null && d.getIsActive())
                .map(dokterService::enrichWithUserData)
                .toList();
    }

    public List<Dokter> getDokterTanpaPoli() {
        List<Dokter> dokters = dokterRepository.findActiveDoktersWithoutPoli();
        return dokterService.enrichAllWithUserData(dokters);
    }

    @Transactional
    @SuppressWarnings("null")
    public void updatePoli(Poli poli, List<String> dokterIds) {
        if (poli == null || poli.getIdPoli() == null) {
            throw new RuntimeException("Poli or Poli ID cannot be null");
        }
        Poli existingPoli = poliRepository.findById(poli.getIdPoli())
                .orElseThrow(() -> new RuntimeException("Poli tidak ditemukan"));
        existingPoli.setNamaPoli(poli.getNamaPoli());
        existingPoli.setLokasiRuangan(poli.getLokasiRuangan());
        poliRepository.save(existingPoli);

        // Disassociate doctors currently in this poli who are not in the new list
        List<Dokter> currentDoctors = dokterRepository.findByPoli_IdPoli(poli.getIdPoli());
        for (Dokter d : currentDoctors) {
            if (dokterIds == null || !dokterIds.contains(d.getIdUser())) {
                d.setPoli(null);
                dokterRepository.save(d);
            }
        }

        // Associate new doctors to this poli
        if (dokterIds != null) {
            for (String docId : dokterIds) {
                if (docId != null) {
                    dokterRepository.findById(docId).ifPresent(d -> {
                        if (d.getPoli() == null || !d.getPoli().getIdPoli().equals(poli.getIdPoli())) {
                            d.setPoli(existingPoli);
                            dokterRepository.save(d);
                        }
                    });
                }
            }
        }
    }

    @Transactional
    public void deletePoli(String poliId) {
        if (poliId == null) {
            throw new RuntimeException("Poli ID cannot be null");
        }
        Poli existingPoli = poliRepository.findById(poliId)
                .orElseThrow(() -> new RuntimeException("Poli tidak ditemukan"));
        existingPoli.setIsActive(false);
        poliRepository.save(existingPoli);

        // Disassociate doctors currently assigned to this poli
        List<Dokter> doctors = dokterRepository.findByPoli_IdPoli(poliId);
        for (Dokter d : doctors) {
            d.setPoli(null);
            dokterRepository.save(d);
        }
    }

    public List<Poli> getAllPoli() {
        List<Poli> allPoli = poliRepository.findAllActive();
        return allPoli.stream()
                .map(p -> {
                    long count = dokterRepository.countActiveDoctorsByPoliId(p.getIdPoli());
                    p.setJumlahDokter((int) count);
                    return p;
                })
                .toList();
    }

    public Optional<Poli> getPoliById(String poliId) {
        if (poliId == null) {
            return Optional.empty();
        }
        return poliRepository.findById(poliId);
    }

    public Long getTotalPoli() {
        return poliRepository.count();
    }
}
