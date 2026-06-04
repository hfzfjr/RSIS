package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.Spesialisasi;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpesialisasiRepository extends JpaRepository<Spesialisasi, String> {

    @Query("SELECT s FROM Spesialisasi s WHERE s.nama LIKE %:nama%")
    List<Spesialisasi> findByNamaContainingIgnoreCase(@Param("nama") String nama);

    @Query("SELECT s FROM Spesialisasi s WHERE s.nama = :nama")
    Optional<Spesialisasi> findByNama(@Param("nama") String nama);
}
