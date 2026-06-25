package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rsis.model.Spesialisasi;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpesialisasiRepository extends JpaRepository<Spesialisasi, String> {

    List<Spesialisasi> findByNamaContainingIgnoreCase(String nama);

    Optional<Spesialisasi> findByNama(String nama);
}
