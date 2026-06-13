package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.WishlistDTO;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.repositories.WishlistRepository;
import online.iwantagift.api.wishlist.util.exceptions.AlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for reading and modifying {@link Wishlist} entities.
 *
 * <p>Regular list updates enforce ownership and prevent modification of the
 * default wishlist. Default-list creation is idempotent and may be called from
 * event consumers or as a fallback when no wishlist id is specified.</p>
 *
 * <p>Update semantics:</p>
 * <ul>
 *   <li>CREATE inserts a new user-owned wishlist.</li>
 *   <li>PUT replaces title and description.</li>
 *   <li>PATCH changes non-null title and description values.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class WishlistService {
    public static final String DEFAULT_WISHLIST_TITLE = "DEFAULT_WISHLIST";
    private static final String DEFAULT_WISHLIST_DESCRIPTION = "Default wishlist";

    private final EntityManager em;
    private final WishlistRepository listRepository;

    /**
     * Creates a new wishlist owned by the requester.
     *
     * <p>Any incoming id is cleared before persistence. The owner id is taken
     * from {@code requestedId}, and the entity is persisted and flushed.</p>
     *
     * @param requestedId authenticated owner id
     * @param wishlist    new wishlist entity to persist
     * @return generated wishlist id
     * @throws AlreadyExistsException if persistence or flush raises a {@link PersistenceException}
     */
    @Transactional
    public UUID create(UUID requestedId, Wishlist wishlist) {
        try {
            wishlist.setOwnerId(requestedId);
            wishlist.setId(null);
            em.persist(wishlist);
            em.flush();
            return wishlist.getId();
        } catch (PersistenceException e) {
            throw new AlreadyExistsException("Wishlist with id %s already exists"
                    .formatted(wishlist.getId()), e);
        }
    }

    /**
     * Applies non-null title and description values to an existing wishlist.
     *
     * <p>The requester must own the wishlist. The system default wishlist
     * cannot be modified.</p>
     *
     * @param requesterId authenticated requester id
     * @param dto         partial update containing the target wishlist id
     * @throws EntityNotFoundException if the target wishlist does not exist
     * @throws ResponseStatusException with status 403 if the requester does not own
     *                                 the wishlist or the target is the default wishlist
     */
    @Transactional
    public void patch(UUID requesterId, WishlistDTO dto) throws EntityNotFoundException {
        Wishlist wl = findByIdOrThrow(dto.getId());

        if (!requesterId.equals(wl.getOwnerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error: requester's id doesn't match to wishlist's owner id");
        if (wl.getTitle().equals(WishlistService.DEFAULT_WISHLIST_TITLE))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error: not allowed to rename default wishlist");

        if (dto.getTitle() != null) wl.setTitle(dto.getTitle());
        if (dto.getDescription() != null) wl.setDescription(dto.getDescription());
    }

    /**
     * Replaces the title and description of an existing wishlist.
     *
     * <p>Null DTO values are assigned as-is. The requester must own the
     * wishlist, and the system default wishlist cannot be modified.</p>
     *
     * @param requesterId authenticated requester id
     * @param dto         complete writable state containing the target wishlist id
     * @throws EntityNotFoundException if the target wishlist does not exist
     * @throws ResponseStatusException with status 403 if the requester does not own
     *                                 the wishlist or the target is the default wishlist
     */
    @Transactional
    public void put(UUID requesterId, WishlistDTO dto) throws EntityNotFoundException {
        Wishlist wl = findByIdOrThrow(dto.getId());

        if (!requesterId.equals(wl.getOwnerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error: requester's id doesn't match to wishlist's owner id");
        if (wl.getTitle().equals(WishlistService.DEFAULT_WISHLIST_TITLE))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error: not allowed to rename default wishlist");

        wl.setTitle(dto.getTitle());
        wl.setDescription(dto.getDescription());
    }

    /**
     * Resolves an explicit wishlist id or the user's default wishlist.
     *
     * @param wishlistId explicit wishlist id, or null to use the default
     * @param userId     owner id used when resolving or creating the default list
     * @return the explicitly requested wishlist or the user's default wishlist
     * @throws EntityNotFoundException if a non-null wishlist id does not exist
     */
    @Transactional
    public Wishlist findByIdOrDefaultIfNull(UUID wishlistId, UUID userId) throws EntityNotFoundException {
        if (wishlistId == null)
            return getOrCreateDefaultList(userId);
        else
            return findByIdOrThrow(wishlistId);
    }

    /**
     * Finds a wishlist by id or fails when it does not exist.
     *
     * @param id wishlist id
     * @return existing wishlist
     * @throws EntityNotFoundException if no wishlist exists with the supplied id
     */
    public Wishlist findByIdOrThrow(UUID id) throws EntityNotFoundException {
        return listRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist with id %s not found".formatted(id)));
    }

    /**
     * Finds a wishlist by id.
     *
     * @param id wishlist id
     * @return the wishlist when found, otherwise an empty optional
     */
    public Optional<Wishlist> findById(UUID id) {
        return listRepository.findById(id);
    }

    /**
     * Returns the user's default wishlist, creating it when absent.
     *
     * <p>Creation is idempotent under concurrent calls because the repository
     * insert resolves conflicts on the unique title and owner pair and returns
     * the database row selected by that key.</p>
     *
     * @param userId owner of the default wishlist
     * @return existing or newly created default wishlist
     */
    @Transactional
    public Wishlist getOrCreateDefaultList(UUID userId) {
        return listRepository.findByOwnerIdAndTitle(userId, DEFAULT_WISHLIST_TITLE)
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setId(UUID.randomUUID());
                    wishlist.setOwnerId(userId);
                    wishlist.setTitle(DEFAULT_WISHLIST_TITLE);
                    wishlist.setDescription(DEFAULT_WISHLIST_DESCRIPTION);
                    return listRepository.idempotentInsert(wishlist);
                });
    }

    /**
     * Deletes a wishlist by id without performing an ownership check.
     *
     * @param id wishlist id
     */
    public void deleteById(UUID id) {
        listRepository.deleteById(id);
    }

    /**
     * Returns all wishlists owned by a user.
     *
     * @param ownerId owner id
     * @return wishlists owned by the user
     */
    public List<Wishlist> findAllByOwnerId(UUID ownerId) {
        return listRepository.findAllByOwnerId(ownerId);
    }

    /**
     * Deletes every wishlist owned by a user.
     *
     * <p>This method performs no requester authorization and deletes lists
     * individually after loading their ids.</p>
     *
     * @param ownerId owner whose wishlists must be deleted
     */
    public void deleteAllByOwnerId(UUID ownerId) {
        findAllByOwnerId(ownerId)
                .stream()
                .map(Wishlist::getId)
                .forEach(this::deleteById);
    }
}
