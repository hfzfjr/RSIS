package rsis.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.Dokter;

import java.util.List;
import java.util.Optional;

@Repository
public interface DokterRepository extends JpaRepository<Dokter, String> {

    @EntityGraph(attributePaths = { "spesialisasi", "poli" })
    @Query("SELECT d FROM Dokter d WHERE d.spesialisasi.nama = :spesialisasi AND (d.isActive IS NULL OR d.isActive = true)")
    List<Dokter> findBySpesialisasi_Nama(@Param("spesialisasi") String spesialisasi);

    @EntityGraph(attributePaths = { "spesialisasi", "poli" })
    @Query("SELECT d FROM Dokter d WHERE d.poli.idPoli = :idPoli AND (d.isActive IS NULL OR d.isActive = true)")
    List<Dokter> findByPoli_IdPoli(@Param("idPoli") String idPoli);

    @EntityGraph(attributePaths = { "spesialisasi", "poli" })
    @Query("SELECT d FROM Dokter d WHERE d.idUser = :idUser AND (d.isActive IS NULL OR d.isActive = true)")
    Optional<Dokter> findByIdUser(@Param("idUser") String idUser);

    @EntityGraph(attributePaths = { "spesialisasi", "poli" })
    @Query("SELECT d FROM Dokter d WHERE d.isActive IS NULL OR d.isActive = true")
    List<Dokter> findAllActive();

    @EntityGraph(attributePaths = { "spesialisasi", "poli" })
    @Query("SELECT d FROM Dokter d WHERE (d.isActive IS NULL OR d.isActive = true) AND (LOWER(d.nama) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.spesialisasi.nama) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.poli.namaPoli) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Dokter> searchBySpesialisasiOrNama(@Param("keyword") String keyword);

    Optional<Dokter> findFirstByNomorStrStartingWithOrderByNomorStrDesc(String prefix);

    @EntityGraph(attributePaths = { "spesialisasi", "poli" })
    @Query("SELECT d FROM Dokter d WHERE d.poli IS NULL AND (d.isActive IS NULL OR d.isActive = true)")
    List<Dokter> findActiveDoktersWithoutPoli();

    @Query("SELECT COUNT(d) FROM Dokter d WHERE d.poli.idPoli = :idPoli AND (d.isActive IS NULL OR d.isActive = true)")
    long countActiveDoctorsByPoliId(@Param("idPoli") String idPoli);
}
