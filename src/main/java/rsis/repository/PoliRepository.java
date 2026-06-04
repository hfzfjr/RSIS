package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.Poli;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoliRepository extends JpaRepository<Poli, String> {

    @Query("SELECT p FROM Poli p WHERE p.namaPoli LIKE %:nama%")
    List<Poli> findByNamaContainingIgnoreCase(@Param("nama") String nama);

    @Query(value = "SELECT id_poli FROM poli WHERE id_poli LIKE 'pli-%' ORDER BY CAST(SUBSTRING(id_poli FROM 5) AS INTEGER) DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestPoliId();
}
