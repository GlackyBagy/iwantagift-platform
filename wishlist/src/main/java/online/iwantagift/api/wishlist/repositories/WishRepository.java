package online.iwantagift.api.wishlist.repositories;

import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WishRepository extends JpaRepository<Wish, UUID> {
    List<Wish> findAllByOwnerId(UUID ow);
}
