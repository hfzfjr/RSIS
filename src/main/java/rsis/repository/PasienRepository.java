package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.Pasien;

import java.util.Optional;

@Repository
public interface PasienRepository extends JpaRepository<Pasien, String> {

    @Query("SELECT p FROM Pasien p WHERE p.idPasien IN (SELECT u.id FROM AppUser u WHERE u.email = :email)")
    Optional<Pasien> findByEmail(@Param("email") String email);

    @Query(value = "SELECT id_pasien FROM pasien WHERE id_pasien LIKE 'psn-%' ORDER BY CAST(SUBSTRING(id_pasien FROM 5) AS INTEGER) DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestPasienId();
}
