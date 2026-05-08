package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rsis.model.Pasien;

import java.util.Optional;

@Repository
public interface PasienRepository extends JpaRepository<Pasien, String> {
    @Query(value = "SELECT id_pasien FROM pasien WHERE id_pasien LIKE 'psn-%' ORDER BY CAST(SUBSTRING(id_pasien FROM 5) AS INTEGER) DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestPasienId();
}
