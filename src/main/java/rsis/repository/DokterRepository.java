package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.Dokter;

import java.util.List;
import java.util.Optional;

@Repository
public interface DokterRepository extends JpaRepository<Dokter, String> {

    List<Dokter> findBySpesialisasi_Nama(String spesialisasi);

    List<Dokter> findByPoli_IdPoli(String idPoli);

    @Query("SELECT d FROM Dokter d WHERE d.idUser = :idUser")
    Optional<Dokter> findByIdUser(@Param("idUser") String idUser);

    @Query("SELECT d FROM Dokter d WHERE LOWER(d.nama) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.spesialisasi.nama) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.poli.namaPoli) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dokter> searchBySpesialisasiOrNama(@Param("keyword") String keyword);

    Optional<Dokter> findFirstByNomorStrStartingWithOrderByNomorStrDesc(String prefix);

    @Query("SELECT d FROM Dokter d WHERE d.poli IS NULL AND (d.isActive IS NULL OR d.isActive = true)")
    List<Dokter> findActiveDoktersWithoutPoli();

    @Query("SELECT COUNT(d) FROM Dokter d WHERE d.poli.idPoli = :idPoli AND (d.isActive IS NULL OR d.isActive = true)")
    long countActiveDoctorsByPoliId(@Param("idPoli") String idPoli);
}
