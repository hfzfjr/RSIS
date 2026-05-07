package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rsis.model.Pasien;

import java.util.Optional;

@Repository
public interface PasienRepository extends JpaRepository<Pasien, String> {
    Optional<Pasien> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query(value = "SELECT id_user FROM pasien WHERE id_user LIKE 'u-%' ORDER BY CAST(SUBSTRING(id_user FROM 3) AS INTEGER) DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestUserId();
}
