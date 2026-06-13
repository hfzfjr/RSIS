package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.Pasien;

import java.util.Optional;

@Repository
public interface PasienRepository extends JpaRepository<Pasien, String> {

    @Query("SELECT p FROM Pasien p WHERE p.idUser = :idUser")
    Optional<Pasien> findByIdUser(@Param("idUser") String idUser);
}
