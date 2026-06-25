package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rsis.model.Pasien;

import java.util.Optional;

@Repository
public interface PasienRepository extends JpaRepository<Pasien, String> {

    Optional<Pasien> findByIdUser(String idUser);

    long count();
}
