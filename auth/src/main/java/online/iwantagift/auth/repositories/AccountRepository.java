package online.iwantagift.auth.repositories;

import online.iwantagift.auth.models.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);

    @Query("SELECT a.id FROM Account a WHERE a.email = :email")
    Optional<UUID> findIdByEmail(String email);
}
