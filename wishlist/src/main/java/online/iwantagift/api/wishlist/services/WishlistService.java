package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.WishlistDTO;
import online.iwantagift.api.wishlist.util.exceptions.AlreadyExistsException;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.repositories.WishlistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service responsible for managing {@link Wishlist} entities.
 *
 * <p>
 * This service defines strict contracts for create, full update (PUT),
 * and partial update (PATCH) operations. The semantics of each operation
 * are explicitly separated to avoid accidental upserts or silent overwrites.
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
public class WishlistService {
    private static final String DEFAULT_WISHLIST_TITLE = "DEFAULT_WISHLIST";
    private static final String DEFAULT_WISHLIST_DESCRIPTION = "Default wishlist";

    private final EntityManager em;
    private final WishlistRepository listRepository;

    /**
     * Creates a new {@link Wishlist} entity and returns generated UUID.
     *
     * <p>
     * This method is <b>insert-only</b>. It uses {@link EntityManager#persist(Object)}
     * to guarantee that a new database row is created.
     * </p>
     *
     * <p>
     * If persistence rejects the insert, the operation fails and an exception
     * is thrown.
     * </p>
     *
     * <h3>Contract</h3>
     * <ul>
     *   <li>The wishlist must represent a new entity.</li>
     *   <li>The owner is assigned from the authenticated user id.</li>
     *   <li>This method must not be used for updates.</li>
     *   <li>On success, a new row is inserted into the database.</li>
     * </ul>
     *
     * @param requestedId authenticated owner id
     * @param wishlist new wishlist entity to persist
     * @throws AlreadyExistsException if persistence rejects the insert as duplicate
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
     * Applies a partial update (PATCH) to an existing wishlist.
     *
     * <p>
     * Only non-null fields in the DTO are applied to the target entity.
     * Fields that are {@code null} are ignored and leave the existing
     * values unchanged. The requester must own the wishlist.
     * </p>
     *
     * <h3>Contract</h3>
     * <ul>
     *   <li>The wishlist must already exist.</li>
     *   <li>Only provided (non-null) fields are updated.</li>
     *   <li>No fields are reset to {@code null} implicitly.</li>
     * </ul>
     *
     * @param requesterId authenticated requester id
     * @param dto PATCH DTO containing partial updates
     * @throws EntityNotFoundException if the target wishlist does not exist
     * @throws org.springframework.web.server.ResponseStatusException if the requester does not own the wishlist
     */
    public void patch(UUID requesterId, WishlistDTO dto) throws EntityNotFoundException {
        Wishlist wl = findByIdOrThrow(dto.getId());

        if (!requesterId.equals(wl.getOwnerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Error: requester's id doesn't match to wishlist's owner id");

        if (dto.getTitle() != null) wl.setTitle(dto.getTitle());
        if (dto.getDescription() != null) wl.setDescription(dto.getDescription());
    }

    /**
     * Applies a full update (PUT) to an existing wishlist.
     *
     * <p>
     * All writable fields in the DTO are treated as part of a full replacement.
     * Depending on the API contract, missing or null fields may overwrite
     * existing values. The requester must own the wishlist.
     * </p>
     *
     * <h3>Contract</h3>
     * <ul>
     *   <li>The wishlist must already exist.</li>
     *   <li>All writable fields are applied.</li>
     *   <li>This method represents full replacement semantics.</li>
     * </ul>
     *
     * @param requesterId authenticated requester id
     * @param dto PUT DTO containing the full state to apply
     * @throws EntityNotFoundException if the target wishlist does not exist
     * @throws ResponseStatusException if the requester does not own the wishlist
     */
    public void put(UUID requesterId, WishlistDTO dto) throws EntityNotFoundException {
        Wishlist wl = findByIdOrThrow(dto.getId());

        if (!requesterId.equals(wl.getOwnerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Error: requester's id doesn't match to wishlist's owner id");

        wl.setTitle(dto.getTitle());
        wl.setDescription(dto.getDescription());
    }

    public Wishlist findByIdOrThrow(UUID id) throws EntityNotFoundException {
        return listRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist with id %s not found".formatted(id)));
    }

    public Optional<Wishlist> findById(UUID id) {
        return listRepository.findById(id);
    }

    @Transactional
    public Wishlist createDefaultList(UUID userId){
        return listRepository.findByOwnerIdAndTitle(userId, DEFAULT_WISHLIST_TITLE)
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setOwnerId(userId);
                    wishlist.setTitle(DEFAULT_WISHLIST_TITLE);
                    wishlist.setDescription(DEFAULT_WISHLIST_DESCRIPTION);
                    return listRepository.save(wishlist);
                });
    }

    public void deleteById(UUID id) {
        listRepository.deleteById(id);
    }

    public List<Wishlist> findAllByOwnerId(UUID ownerId) {
        return listRepository.findAllByOwnerId(ownerId);
    }
}
