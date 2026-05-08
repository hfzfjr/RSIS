package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rsis.model.AppUser;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, String> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query(value = "SELECT id_user FROM users WHERE id_user LIKE 'usr-%' ORDER BY CAST(SUBSTRING(id_user FROM 5) AS INTEGER) DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestUserId();
}
