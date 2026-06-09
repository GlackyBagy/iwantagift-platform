package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.repositories.WishRepository;
import online.iwantagift.api.wishlist.util.exceptions.AlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service responsible for managing {@link Wish} entities.
 *
 * <p>
 * This service defines strict contracts for create, full update (PUT),
 * and partial update (PATCH) operations on wishes.
 * </p>
 *
 * <h2>Write Semantics</h2>
 * <ul>
 *   <li><b>CREATE</b> — insert-only. Fails if the entity already exists.</li>
 *   <li><b>PUT</b> — full replacement of an existing entity.</li>
 *   <li><b>PATCH</b> — partial update of an existing entity.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class WishService {
    private final EntityManager em;
    private final WishRepository wishRepository;
    private final WishlistService wishlistService;

    /**
     * Creates a new {@link Wish} entity.
     *
     * <p>
     * This method is <b>insert-only</b> and guarantees that a new wish
     * is persisted using {@link EntityManager#persist(Object)} and returns generated UUID.
     * </p>
     *
     * <h3>Contract</h3>
     * <ul>
     *   <li>The wish must represent a new entity.</li>
     *   <li>The owner is assigned from the authenticated user id.</li>
     *   <li>If persistence rejects the insert, the operation fails.</li>
     *   <li>This method must not be used for updates.</li>
     * </ul>
     *
     * @param ownerId authenticated owner id
     * @param wish new wish entity to persist
     * @throws AlreadyExistsException if persistence rejects the insert as duplicate
     */
    @Transactional
    public UUID create(UUID ownerId, Wish wish) {
        try {
            wish.setOwnerId(ownerId);
            attachManagedWishlist(wish);
            em.persist(wish);
            em.flush();
            return wish.getId();
        } catch (PersistenceException e) {
            throw new AlreadyExistsException("Wish with id %s already exists"
                    .formatted(wish.getId()), e);
        }
    }

    private void attachManagedWishlist(Wish wish) {
        if (wish.getWishlist() == null || wish.getWishlist().getId() == null)
            return;

        wish.setWishlist(wishlistService.findByIdOrThrow(wish.getWishlist().getId()));
    }

    /**
     * Applies a partial update (PATCH) to an existing wish.
     *
     * <p>
     * Only non-null fields in the DTO are applied.
     * Existing values are preserved for omitted fields.
     * The requester must own the wish and any target wishlist.
     * </p>
     *
     * @param requesterId authenticated requester id
     * @param dto PATCH DTO
     * @throws EntityNotFoundException if the target wish does not exist
     * @throws ResponseStatusException with 403 if the requester does not own the resource
     * @throws ResponseStatusException with 404 if the target wishlist does not exist
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
            boolean allowed = wishlistService
                    .findById(dto.getWishListId())
                    .map(x -> requesterId.equals(x.getOwnerId()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Error: Specified wishlist not found."));

            if (!allowed)
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);

            wish.setWishlist(
                    wishlistService.findByIdOrThrow(dto.getWishListId())
            );
        }
    }

    /**
     * Applies a full update (PUT) to an existing wish.
     *
     * <p>All writable fields are applied as part of a full replacement.</p>
     *
     * <p><b>Validation:</b> This method does not validate {@code dto}. It assumes that
     * input was validated earlier (e.g., at the API layer). If {@code dto} contains invalid
     * values (nulls, too-long strings, etc.), persistence/transaction commit may fail.</p>
     *
     * @param requesterId authenticated requester id
     * @param dto PUT DTO (assumed to be validated)
     * @throws EntityNotFoundException                  if the target wish or the target wishlist does not exist
     * @throws ResponseStatusException                  with 403 if the requester does not own the wish
     * @throws jakarta.persistence.PersistenceException if the update violates database constraints
     *                                                  (propagated from the persistence layer, typically on flush/commit)
     */
    public void put(UUID requesterId, WishDTO dto) throws EntityNotFoundException {
        Wish wish = findByIdOrThrow(dto.getId());

        if (!requesterId.equals(wish.getOwnerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error: requester's id doesn't match to wish's owner id");

        wish.setTitle(dto.getTitle());
        wish.setDescription(dto.getDescription());
        wish.setUrl(dto.getUrl());
        wish.setWishlist(wishlistService.findByIdOrThrow(dto.getWishListId()));
    }

    public Optional<Wish> findById(UUID id) {
        return wishRepository.findById(id);
    }

    public Wish findByIdOrThrow(UUID id) {
        return wishRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wish with id %s not found".formatted(id)));
    }

    public void deleteById(UUID id) {
        wishRepository.deleteById(id);
    }

    public List<Wish> findAllByOwnerId(UUID ownerId) {
        return wishRepository.findAllByOwnerId(ownerId);
    }
}
