package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rsis.model.AdminRS;

import java.util.Optional;

@Repository
public interface AdminRSRepository extends JpaRepository<AdminRS, String> {

    Optional<AdminRS> findByIdUser(String idUser);
}
