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

    @Query("SELECT d FROM Dokter d WHERE d.idDokter IN (SELECT u.id FROM AppUser u WHERE u.email = :email)")
    Optional<Dokter> findByEmail(@Param("email") String email);

    @Query("SELECT d FROM Dokter d WHERE d.idDokter IN (SELECT u.id FROM AppUser u WHERE u.nama LIKE %:keyword%) OR d.idDokter IN (SELECT u.id FROM AppUser u WHERE u.email LIKE %:keyword%)")
    List<Dokter> searchBySpesialisasiOrNama(@Param("keyword") String keyword);

    @Query(value = "SELECT id_dokter FROM dokter WHERE id_dokter LIKE 'dkt-%' ORDER BY CAST(SUBSTRING(id_dokter FROM 5) AS INTEGER) DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestDokterId();
}
