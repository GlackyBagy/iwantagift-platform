package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
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

@Service
@RequiredArgsConstructor
public class WishService {
    @PersistenceContext
    private EntityManager em;
    private final WishRepository wishRepository;
    private final WishlistService wishlistService;

    @Transactional
    public void create(Wish wish) {
        try {
            em.persist(wish);
            em.flush();
        } catch (PersistenceException e) {
            throw new AlreadyExistsException("Wish with id %s already exists"
                    .formatted(wish.getId()), e);
        }
    }

    @Transactional
    public void update(WishWriteDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("Dto is null");

        if (dto instanceof WishPatchDTO)
            patch((WishPatchDTO) dto);
        else if (dto instanceof WishPutDTO)
            put((WishPutDTO) dto);
        else
            throw new IllegalArgumentException("Dto type %s not supported".formatted(dto.getClass()));
    }

    private void patch(WishPatchDTO dto) {
        Wish wish = findByIdOrThrow(dto.getId());

        if (dto.getTitle() != null) wish.setTitle(dto.getTitle());
        if (dto.getDescription() != null) wish.setDescription(dto.getDescription());
        if (dto.getUrl() != null) wish.setUrl(dto.getUrl());
        if (dto.getWishListId() != null)
            wish.setWishlist(
                    wishlistService.findByIdOrThrow(dto.getWishListId())
            );
    }

    private void put(WishPutDTO dto) {
        Wish wish = findByIdOrThrow(dto.getId());

        wish.setTitle(dto.getTitle());
        wish.setDescription(dto.getDescription());
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
