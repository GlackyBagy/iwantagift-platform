package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.exceptions.AlreadyExistsException;
import online.iwantagift.api.wishlist.models.dto.abstracts.WishlistWriteDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistPatchDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistPutDTO;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.repositories.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @PersistenceContext
    private EntityManager em;
    private final WishlistRepository listRepository;

    /**
     * Creates a new {@link Wishlist} entity.
     *
     * <p>
     * This method is <b>insert-only</b>. It uses {@link EntityManager#persist(Object)}
     * to guarantee that a new database row is created.
     * </p>
     *
     * <p>
     * If an entity with the same identifier already exists, the operation
     * fails and an exception is thrown.
     * </p>
     *
     * <h3>Contract</h3>
     * <ul>
     *   <li>The wishlist must represent a new entity.</li>
     *   <li>This method must not be used for updates.</li>
     *   <li>On success, a new row is inserted into the database.</li>
     * </ul>
     *
     * @param wishlist new wishlist entity to persist
     * @throws AlreadyExistsException if a wishlist with the same id already exists
     */
    public void create(Wishlist wishlist) {
        try {
            em.persist(wishlist);
            em.flush();
        } catch (PersistenceException e) {
            throw new AlreadyExistsException("Wishlist with id %s already exists"
                    .formatted(wishlist.getId()), e);
        }
    }

    /**
     * Updates an existing wishlist using a write DTO.
     *
     * <p>
     * This method acts as a dispatcher based on the concrete DTO type
     * and delegates to either PUT or PATCH semantics.
     * </p>
     *
     * <h3>Dispatch Rules</h3>
     * <ul>
     *   <li>{@link WishlistPatchDTO} → partial update (PATCH)</li>
     *   <li>{@link WishlistPutDTO} → full replacement (PUT)</li>
     * </ul>
     *
     * @param dto WriteDTO describing the update operation
     * @throws IllegalArgumentException if dto is null or of unsupported type
     * @throws EntityNotFoundException if the target wishlist does not exist
     */
    @Transactional
    public void update(WishlistWriteDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("Dto is null");

        if (dto instanceof WishlistPatchDTO)
            patch((WishlistPatchDTO) dto);
        else if (dto instanceof WishlistPutDTO)
            put((WishlistPutDTO) dto);
        else
            throw new IllegalArgumentException("Dto type %s not supported".formatted(dto.getClass()));
    }

    /**
     * Applies a partial update (PATCH) to an existing wishlist.
     *
     * <p>
     * Only non-null fields in the DTO are applied to the target entity.
     * Fields that are {@code null} are ignored and leave the existing
     * values unchanged.
     * </p>
     *
     * <h3>Contract</h3>
     * <ul>
     *   <li>The wishlist must already exist.</li>
     *   <li>Only provided (non-null) fields are updated.</li>
     *   <li>No fields are reset to {@code null} implicitly.</li>
     * </ul>
     *
     * @param dto PATCH DTO containing partial updates
     * @throws EntityNotFoundException if the target wishlist does not exist
     */
    private void patch(WishlistPatchDTO dto) {
        Wishlist wl = findByIdOrThrow(dto.getId());

        if (dto.getTitle() != null) wl.setTitle(dto.getTitle());
        if (dto.getDescription() != null) wl.setDescription(dto.getDescription());
    }

    /**
     * Applies a full update (PUT) to an existing wishlist.
     *
     * <p>
     * All writable fields in the DTO are treated as part of a full replacement.
     * Depending on the API contract, missing or null fields may overwrite
     * existing values.
     * </p>
     *
     * <h3>Contract</h3>
     * <ul>
     *   <li>The wishlist must already exist.</li>
     *   <li>All writable fields are applied.</li>
     *   <li>This method represents full replacement semantics.</li>
     * </ul>
     *
     * @param dto PUT DTO containing the full state to apply
     * @throws EntityNotFoundException if the target wishlist does not exist
     */
    private void put(WishlistPutDTO dto) {
        Wishlist wl = findByIdOrThrow(dto.getId());

        wl.setTitle(dto.getTitle());
        wl.setDescription(dto.getDescription());
    }

    public Wishlist findByIdOrThrow(UUID id) {
        return listRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist with id %s not found".formatted(id)));
    }

    public Optional<Wishlist> findById (UUID id) {
        return listRepository.findById(id);
    }

    public void deleteById(UUID id) {
        listRepository.deleteById(id);
    }

    public List<Wishlist> findAllByOwnerId(UUID ownerId) {
        return listRepository.findAllByOwnerId(ownerId);
    }
}
