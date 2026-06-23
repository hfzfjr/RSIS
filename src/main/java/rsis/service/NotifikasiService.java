package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.User;
import rsis.model.Notifikasi;
import rsis.repository.UserRepository;
import rsis.repository.NotifikasiRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class NotifikasiService {

    @Autowired
    private NotifikasiRepository notifikasiRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Notifikasi kirimNotifikasi(String penerimaId, String pesan, String tipe) {
        Optional<User> penerimaOpt = userRepository.findById(penerimaId);
        if (penerimaOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Notifikasi notifikasi = new Notifikasi();
        notifikasi.setIdNotifikasi(generateNotifikasiId());
        notifikasi.setPenerima(penerimaOpt.get());
        notifikasi.setPesan(pesan);
        notifikasi.setTipe(tipe);
        notifikasi.setTanggalKirim(Instant.now());
        notifikasi.setStatus("BELUM_DIBACA");
        return notifikasiRepository.save(notifikasi);
    }

    public List<Notifikasi> getNotifikasiByPenerimaId(String penerimaId) {
        return notifikasiRepository.findByPenerima_IdUserOrderByTanggalKirimDesc(penerimaId);
    }

    @Transactional
    public void tandaiDibaca(String notifikasiId) {
        notifikasiRepository.findById(notifikasiId).ifPresent(notif -> {
            notif.markAsRead();
            notifikasiRepository.save(notif);
        });
    }

    @Transactional
    public void deleteNotifikasi(String notifikasiId) {
        notifikasiRepository.deleteById(notifikasiId);
    }

    @Transactional
    public void markAsRead(String notifikasiId) {
        notifikasiRepository.findById(notifikasiId).ifPresent(notif -> {
            notif.markAsRead();
            notifikasiRepository.save(notif);
        });
    }

    public Long getUnreadCount(String penerimaId) {
        return notifikasiRepository.countByPenerima_IdUserAndStatus(penerimaId, "BELUM_DIBACA");
    }

    private String generateNotifikasiId() {
        long count = notifikasiRepository.count();
        int nextNumber = (int) (count + 1);
        return String.format("ntf-%03d", nextNumber);
    }
}
