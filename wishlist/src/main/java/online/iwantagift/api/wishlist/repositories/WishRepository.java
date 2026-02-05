package online.iwantagift.api.wishlist.repositories;

import online.iwantagift.api.wishlist.models.entities.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WishRepository extends JpaRepository<Wish, UUID> {
    @Query(value = "select * from wishes where owner_id = :ow", nativeQuery = true)
    List<Wish> findAllByOwnerId(UUID ow);
}
