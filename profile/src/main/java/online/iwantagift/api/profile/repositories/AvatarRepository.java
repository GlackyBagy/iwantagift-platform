package online.iwantagift.api.profile.repositories;

import online.iwantagift.api.profile.models.entities.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, UUID> {
}
