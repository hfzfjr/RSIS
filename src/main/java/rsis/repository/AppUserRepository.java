package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rsis.model.User;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query(value = "select id_user from users where id_user like 'u-%' order by cast(substring(id_user, 3) as int) desc limit 1", nativeQuery = true)
    Optional<String> findLatestUserId();
}
