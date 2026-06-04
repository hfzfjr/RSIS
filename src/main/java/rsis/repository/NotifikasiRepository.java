package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rsis.model.Notifikasi;

import java.util.List;

@Repository
public interface NotifikasiRepository extends JpaRepository<Notifikasi, String> {

    List<Notifikasi> findByPenerima_IdUser(String penerimaId);

    List<Notifikasi> findByPenerima_IdUserOrderByTanggalKirimDesc(String penerimaId);
}
