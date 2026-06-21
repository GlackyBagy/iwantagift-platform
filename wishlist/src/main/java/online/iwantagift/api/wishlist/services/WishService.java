package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.repositories.WishRepository;
import online.iwantagift.api.wishlist.util.exceptions.AlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Contract;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for reading and modifying {@link Wish} entities.
 *
 * <p>Write operations enforce ownership of the affected wish and wishlist.
 * Persistence and transaction management are handled in this layer; HTTP
 * controllers are responsible for request validation and authentication.</p>
 *
 * <p>Update semantics:</p>
 * <ul>
 *   <li>CREATE inserts a wish into an existing wishlist owned by the requester.</li>
 *   <li>PUT replaces writable fields and uses the default wishlist when no list is specified.</li>
 *   <li>PATCH changes non-null fields and preserves the current wishlist when no list is specified.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class WishService {
    private final EntityManager em;
    private final WishRepository wishRepository;
    private final WishlistService wishlistService;

    /**
     * Creates a wish in the wishlist referenced by {@code wish}.
     *
     * <p>The referenced wishlist is loaded from persistence and must belong to
     * the requester. The managed wishlist instance and requester id are assigned
     * to the wish before it is persisted and flushed.</p>
     *
     * @param requesterId authenticated owner id
     * @param wish        new wish with a non-null wishlist id
     * @return generated wish id
     * @throws EntityNotFoundException if the referenced wishlist does not exist
     * @throws ResponseStatusException with status 403 if the wishlist belongs to another user
     * @throws AlreadyExistsException  if persistence or flush raises a {@link PersistenceException}
     */
    @Transactional
    public UUID create(UUID requesterId, Wish wish) {
        Wishlist wishlist = wishlistService.findByIdOrThrow(wish.getWishlist().getId());

        if (!requesterId.equals(wishlist.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wishlist belongs to another user");
        }

        try {
            wish.setOwnerId(requesterId);
            wish.setWishlist(wishlist);
            em.persist(wish);
            em.flush();
            return wish.getId();
        } catch (PersistenceException e) {
            throw new AlreadyExistsException("Wish with id %s already exists"
                    .formatted(wish.getId()), e);
        }
    }

    /**
     * Applies non-null DTO fields to an existing wish.
     *
     * <p>The requester must own the wish. When {@code wishListId} is present,
     * the target wishlist must exist and also belong to the requester. A null
     * {@code wishListId} leaves the current wishlist unchanged.</p>
     *
     * @param requesterId authenticated requester id
     * @param dto         partial update containing the target wish id
     * @throws EntityNotFoundException if the target wish does not exist
     *                                 or the requested wishlist does not exist
     * @throws ResponseStatusException with status 403 if the requester does not own
     *                                 the wish or requested wishlist
     */
    @Transactional
    public void patch(UUID requesterId, WishDTO dto) throws EntityNotFoundException {
        Wish wish = findByIdOrThrow(dto.getId());

        if (!requesterId.equals(wish.getOwnerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error: requester's id doesn't match to wish's owner id");

        if (dto.getTitle() != null) wish.setTitle(dto.getTitle());
        if (dto.getDescription() != null) wish.setDescription(dto.getDescription());
        if (dto.getUrl() != null) wish.setUrl(dto.getUrl());
        if (dto.getWishListId() != null) {
            Wishlist requestedList = wishlistService.findByIdOrThrow(dto.getWishListId());

            if (!isWishlistReassignAllowed(requesterId, requestedList))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);

            wish.setWishlist(requestedList);
        }
    }

    /**
     * Replaces all writable fields of an existing wish.
     *
     * <p>The requester must own the wish and an explicitly requested wishlist.
     * When {@code wishListId} is null, the requester's default wishlist is found
     * or created and assigned to the wish. DTO validation is expected to happen
     * before this method is called.</p>
     *
     * @param requesterId authenticated requester id
     * @param dto         complete writable state containing the target wish id
     * @throws EntityNotFoundException if the target wish or explicitly requested wishlist does not exist
     * @throws ResponseStatusException with status 403 if the requester does not own
     *                                 the wish or explicitly requested wishlist
     * @throws PersistenceException    if the resulting state violates a persistence constraint
     */
    @Transactional
    public void put(UUID requesterId, WishDTO dto) throws EntityNotFoundException {
        Wish wish = findByIdOrThrow(dto.getId());

        if (!requesterId.equals(wish.getOwnerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error: requester's id doesn't match to wish's owner id");

        if (!isWishlistReassignAllowed(requesterId, dto.getWishListId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        wish.setWishlist(
                wishlistService.findByIdOrDefaultIfNull(dto.getWishListId(), requesterId)
        );
        wish.setTitle(dto.getTitle());
        wish.setDescription(dto.getDescription());
        wish.setUrl(dto.getUrl());
    }

    @Contract(value = "_, null -> true")
    private boolean isWishlistReassignAllowed(UUID requesterId, UUID wishlistId) {
        return wishlistId == null || isWishlistReassignAllowed(requesterId, wishlistService.findByIdOrThrow(wishlistId));
    }

    private boolean isWishlistReassignAllowed(UUID requesterId, Wishlist list) {
        return Objects.equals(requesterId, list.getOwnerId());
    }

    /**
     * Finds a wish by id.
     *
     * @param id wish id
     * @return the wish when found, otherwise an empty optional
     */
    public Optional<Wish> findById(UUID id) {
        return wishRepository.findById(id);
    }

    /**
     * Finds a wish by id or fails when it does not exist.
     *
     * @param id wish id
     * @return existing wish
     * @throws EntityNotFoundException if no wish exists with the supplied id
     */
    public Wish findByIdOrThrow(UUID id) {
        return wishRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wish with id %s not found".formatted(id)));
    }

    /**
     * Deletes a wish by id without performing an ownership check.
     *
     * @param id wish id
     */
    public void deleteById(UUID id) {
        wishRepository.deleteById(id);
    }

    /**
     * Returns all wishes owned by a user.
     *
     * @param ownerId owner id
     * @return wishes owned by the user
     */
    public List<Wish> findAllByOwnerId(UUID ownerId) {
        return wishRepository.findAllByOwnerId(ownerId);
    }
}
