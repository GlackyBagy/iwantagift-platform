package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.exceptions.AlreadyExistsException;
import online.iwantagift.api.wishlist.models.dto.WishPatchDTO;
import online.iwantagift.api.wishlist.models.dto.WishPutDTO;
import online.iwantagift.api.wishlist.models.dto.abstracts.WishWriteDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.repositories.WishRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     *   <li>If a wish with the same id already exists, the operation fails.</li>
     *   <li>This method must not be used for updates.</li>
     * </ul>
     *
     * @param wish new wish entity to persist
     * @throws AlreadyExistsException if a wish with the same id already exists
     */
    @Transactional
    public UUID create(Wish wish) {
        try {
            wish.setId(null);
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
     * Updates an existing wish using a write DTO.
     *
     * <p>
     * Dispatches to PATCH or PUT semantics based on the concrete DTO type.
     * </p>
     *
     * @param dto WriteDTO describing the update operation
     * @throws IllegalArgumentException if dto is null or of unsupported type
     * @throws EntityNotFoundException if the target wish does not exist
     */
    @Transactional
    public void update(WishWriteDTO dto) throws EntityNotFoundException {
        if (dto == null)
            throw new IllegalArgumentException("Dto is null");

        if (dto instanceof WishPatchDTO)
            patch((WishPatchDTO) dto);
        else if (dto instanceof WishPutDTO)
            put((WishPutDTO) dto);
        else
            throw new IllegalArgumentException("Dto type %s not supported".formatted(dto.getClass()));
    }

    /**
     * Applies a partial update (PATCH) to an existing wish.
     *
     * <p>
     * Only non-null fields in the DTO are applied.
     * Existing values are preserved for omitted fields.
     * </p>
     *
     * @param dto PATCH DTO
     * @throws EntityNotFoundException if the target wish or the target wishlist does not exist
     */
    private void patch(WishPatchDTO dto) throws EntityNotFoundException {
        Wish wish = findByIdOrThrow(dto.getId());

        if (dto.getTitle() != null) wish.setTitle(dto.getTitle());
        if (dto.getDescription() != null) wish.setDescription(dto.getDescription());
        if (dto.getUrl() != null) wish.setUrl(dto.getUrl());
        if (dto.getWishListId() != null)
            wish.setWishlist(
                    wishlistService.findByIdOrThrow(dto.getWishListId()) // todo reject on stranger's wishlist
            );
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
     * @param dto PUT DTO (assumed to be validated)
     * @throws EntityNotFoundException if the target wish or the target wishlist does not exist
     * @throws jakarta.persistence.PersistenceException if the update violates database constraints
     *         (propagated from the persistence layer, typically on flush/commit)
     */
    private void put(WishPutDTO dto) throws EntityNotFoundException {
        Wish wish = findByIdOrThrow(dto.getId());

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
