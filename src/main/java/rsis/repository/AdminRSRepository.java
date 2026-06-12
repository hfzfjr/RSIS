package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.AdminRS;

import java.util.Optional;

@Repository
public interface AdminRSRepository extends JpaRepository<AdminRS, String> {

    @Query("SELECT a FROM AdminRS a WHERE a.idUser = :idUser")
    Optional<AdminRS> findByIdUser(@Param("idUser") String idUser);
}
