package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.AdminRS;

import java.util.Optional;

@Repository
public interface AdminRSRepository extends JpaRepository<AdminRS, String> {

    @Query("SELECT a FROM AdminRS a WHERE a.idAdmin IN (SELECT u.id FROM AppUser u WHERE u.email = :email)")
    Optional<AdminRS> findByEmail(@Param("email") String email);
}
