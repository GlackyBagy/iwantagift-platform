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

@Service
@RequiredArgsConstructor
public class WishlistService {
    @PersistenceContext
    private EntityManager em;
    private final WishlistRepository listRepository;

    public void create(Wishlist wishlist) {
        try {
            em.persist(wishlist);
            em.flush();
        } catch (PersistenceException e) {
            throw new AlreadyExistsException("Wishlist with id %s already exists"
                    .formatted(wishlist.getId()), e);
        }
    }

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

    private void patch(WishlistPatchDTO dto) {
        Wishlist wl = findByIdOrThrow(dto.getId());

        if (dto.getTitle() != null) wl.setTitle(dto.getTitle());
        if (dto.getDescription() != null) wl.setDescription(dto.getDescription());
    }

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
