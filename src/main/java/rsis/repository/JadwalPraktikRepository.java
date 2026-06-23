package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.JadwalPraktik;

import java.util.List;

@Repository
public interface JadwalPraktikRepository extends JpaRepository<JadwalPraktik, String> {

    @Query(value = "SELECT id_jadwal FROM jadwal_praktik WHERE id_jadwal LIKE 'jdw-%' ORDER BY CAST(SUBSTRING(id_jadwal FROM 5) AS INTEGER) DESC LIMIT 1", nativeQuery = true)
    java.util.Optional<String> findLatestJadwalId();

    List<JadwalPraktik> findByDokter_IdUser(String idUser);

    List<JadwalPraktik> findByDokter_Poli_IdPoli(String idPoli);

    List<JadwalPraktik> findByHari(String hari);

    List<JadwalPraktik> findByDokter_IdUserAndHari(String idUser, String hari);

    @Query("SELECT j FROM JadwalPraktik j WHERE (j.isActive IS NULL OR j.isActive = true) AND j.dokter.idUser = :dokterId AND j.sisaKuota > 0 AND j.statusKetersediaan = 'TERSEDIA' ORDER BY j.hari ASC, j.jamMulai ASC")
    List<JadwalPraktik> findAvailableJadwalByDokterId(@Param("dokterId") String dokterId);

    @Query("SELECT j FROM JadwalPraktik j WHERE (j.isActive IS NULL OR j.isActive = true) AND j.sisaKuota > 0 AND j.statusKetersediaan = 'TERSEDIA' ORDER BY j.hari ASC, j.jamMulai ASC")
    List<JadwalPraktik> findAllAvailableJadwal();

    @Query("SELECT j FROM JadwalPraktik j WHERE (j.isActive IS NULL OR j.isActive = true) AND j.dokter.poli.idPoli = :poliId AND j.sisaKuota > 0 AND j.statusKetersediaan = 'TERSEDIA' ORDER BY j.hari ASC, j.jamMulai ASC")
    List<JadwalPraktik> findAvailableJadwalByPoliId(@Param("poliId") String poliId);
}
