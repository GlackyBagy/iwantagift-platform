package online.iwantagift.api.profile.repositories;

import online.iwantagift.api.profile.models.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
}
