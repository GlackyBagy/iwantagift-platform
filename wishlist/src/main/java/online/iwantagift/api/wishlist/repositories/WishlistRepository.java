package online.iwantagift.api.wishlist.repositories;

import online.iwantagift.api.wishlist.models.entities.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    List<Wishlist> findAllByOwnerId(UUID ow);

    Optional<Wishlist> findByOwnerIdAndTitle(UUID ownerId, String title);

    @Query(
            value = """
                    INSERT INTO wishlist (id, title, description, owner_id)
                               VALUES (
                                   :#{#wishlist.id},
                                   :#{#wishlist.title},
                                   :#{#wishlist.description},
                                   :#{#wishlist.ownerId}
                               )
                               ON CONFLICT (title, owner_id)
                               DO UPDATE SET title = EXCLUDED.title
                               RETURNING *
                    """, nativeQuery = true
    )
    @Modifying
    Wishlist idempotentInsert(Wishlist wishlist);
}
