package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.Notifikasi;

import java.util.List;

@Repository
public interface NotifikasiRepository extends JpaRepository<Notifikasi, String> {

    @Query(value = "SELECT * FROM notifikasi WHERE id_user = :penerimaId ORDER BY tanggal_kirim DESC", nativeQuery = true)
    List<Notifikasi> findByPenerima_IdUserOrderByTanggalKirimDesc(@Param("penerimaId") String penerimaId);

    @Query(value = "SELECT * FROM notifikasi WHERE id_user = :penerimaId", nativeQuery = true)
    List<Notifikasi> findByPenerima_IdUser(@Param("penerimaId") String penerimaId);

    @Query(value = "SELECT COUNT(*) FROM notifikasi WHERE id_user = :penerimaId AND status = :status", nativeQuery = true)
    Long countByPenerima_IdUserAndStatus(@Param("penerimaId") String penerimaId, @Param("status") String status);
}
