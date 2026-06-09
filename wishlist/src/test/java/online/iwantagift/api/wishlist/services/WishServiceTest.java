package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.repositories.WishRepository;
import online.iwantagift.api.wishlist.util.exceptions.AlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishServiceTest {
    @Mock
    private EntityManager em;
    @Mock
    private WishRepository wr;
    @Mock
    private WishlistService wls;

    @InjectMocks
    private WishService wishService;

    @Test
    void create_setsOwnerIdAndPersistsWish() {
        UUID ownerId = UUID.randomUUID();
        Wish wish = new Wish();
        wish.setTitle("title");
        wish.setDescription("new description");

        doAnswer(invocation -> {
            wish.setId(UUID.randomUUID());
            return null;
        }).when(em).flush();

        UUID id = wishService.create(ownerId, wish);

        assertNotNull(id);
        assertEquals(ownerId, wish.getOwnerId());
        verify(em).persist(wish);
        verify(em).flush();
        verifyNoInteractions(wr, wls);
    }

    @Test
    void create_attachesManagedWishlistBeforePersist() {
        UUID ownerId = UUID.randomUUID();
        UUID wishlistId = UUID.randomUUID();
        Wishlist detachedWishlist = new Wishlist();
        detachedWishlist.setId(wishlistId);

        Wishlist managedWishlist = new Wishlist();
        managedWishlist.setId(wishlistId);

        Wish wish = new Wish();
        wish.setTitle("title");
        wish.setWishlist(detachedWishlist);

        when(wls.findByIdOrThrow(wishlistId)).thenReturn(managedWishlist);
        doAnswer(invocation -> {
            wish.setId(UUID.randomUUID());
            return null;
        }).when(em).flush();

        wishService.create(ownerId, wish);

        assertEquals(ownerId, wish.getOwnerId());
        assertSame(managedWishlist, wish.getWishlist());
        verify(wls).findByIdOrThrow(wishlistId);
        verify(em).persist(wish);
        verify(em).flush();
        verifyNoInteractions(wr);
    }

    @Test
    void create_whenAlreadyExists_throwsAlreadyExistsException() {
        UUID ownerId = UUID.randomUUID();
        Wish wish = new Wish();
        wish.setId(UUID.randomUUID());

        doThrow(new PersistenceException("duplicated key")).when(em).persist(wish);

        assertThrows(AlreadyExistsException.class, () -> wishService.create(ownerId, wish));
    }

    @Test
    void patch_whenRequesterOwnsWish_updatesOnlyNonNullFields() {
        UUID ownerId = UUID.randomUUID();
        Wishlist oldWishlist = wishlist(UUID.randomUUID());
        Wishlist newWishlist = wishlist(UUID.randomUUID());
        Wish existing = new Wish(UUID.randomUUID(), "old title", "old description",
                "http://old.url", null, oldWishlist, ownerId);

        WishDTO dto = new WishDTO();
        dto.setId(existing.getId());
        dto.setTitle("new title");
        dto.setDescription("new description");
        dto.setUrl("http://new.url");
        dto.setWishListId(newWishlist.getId());

        when(wr.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(wls.findByIdOrThrow(newWishlist.getId())).thenReturn(newWishlist);

        wishService.patch(ownerId, dto);

        assertEquals("new title", existing.getTitle());
        assertEquals("new description", existing.getDescription());
        assertEquals("http://new.url", existing.getUrl());
        assertSame(newWishlist, existing.getWishlist());
        verify(wr).findById(existing.getId());
        verify(wls).findByIdOrThrow(newWishlist.getId());
        verifyNoInteractions(em);
    }

    @Test
    void patch_whenDtoFieldsAreNull_preservesExistingValues() {
        UUID ownerId = UUID.randomUUID();
        Wishlist oldWishlist = wishlist(UUID.randomUUID());
        Wish existing = new Wish(UUID.randomUUID(), "old title", "old description",
                "http://old.url", null, oldWishlist, ownerId);

        WishDTO dto = new WishDTO();
        dto.setId(existing.getId());

        when(wr.findById(existing.getId())).thenReturn(Optional.of(existing));

        wishService.patch(ownerId, dto);

        assertEquals("old title", existing.getTitle());
        assertEquals("old description", existing.getDescription());
        assertEquals("http://old.url", existing.getUrl());
        assertSame(oldWishlist, existing.getWishlist());
        verify(wr).findById(existing.getId());
        verifyNoInteractions(wls, em);
    }

    @Test
    void patch_whenRequesterDoesNotOwnWish_throwsUnauthorizedException() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Wish existing = new Wish(UUID.randomUUID(), "title", null, "http://url", null, null, ownerId);

        WishDTO dto = new WishDTO();
        dto.setId(existing.getId());
        dto.setTitle("new title");

        when(wr.findById(existing.getId())).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () -> wishService.patch(requesterId, dto));
        verify(wr).findById(existing.getId());
        verifyNoInteractions(wls, em);
    }

    @Test
    void put_whenRequesterOwnsWish_replacesWritableFields() {
        UUID ownerId = UUID.randomUUID();
        Wishlist oldWishlist = wishlist(UUID.randomUUID());
        Wishlist newWishlist = wishlist(UUID.randomUUID());
        Wish existing = new Wish(UUID.randomUUID(), "old title", "old description",
                "http://old.url", null, oldWishlist, ownerId);

        WishDTO dto = new WishDTO();
        dto.setId(existing.getId());
        dto.setTitle("new title");
        dto.setDescription(null);
        dto.setUrl("http://new.url");
        dto.setWishListId(newWishlist.getId());

        when(wr.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(wls.findByIdOrThrow(newWishlist.getId())).thenReturn(newWishlist);

        wishService.put(ownerId, dto);

        assertEquals("new title", existing.getTitle());
        assertNull(existing.getDescription());
        assertEquals("http://new.url", existing.getUrl());
        assertSame(newWishlist, existing.getWishlist());
        verify(wr).findById(existing.getId());
        verify(wls).findByIdOrThrow(newWishlist.getId());
        verifyNoInteractions(em);
    }

    @Test
    void put_whenRequesterDoesNotOwnWish_throwsUnauthorizedException() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Wish existing = new Wish(UUID.randomUUID(), "title", null, "http://url", null, null, ownerId);

        WishDTO dto = new WishDTO();
        dto.setId(existing.getId());
        dto.setTitle("new title");

        when(wr.findById(existing.getId())).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () -> wishService.put(requesterId, dto));
        verify(wr).findById(existing.getId());
        verifyNoInteractions(wls, em);
    }

    @Test
    void patch_whenWishNotExists_throwsEntityNotFoundException() {
        WishDTO dto = new WishDTO();
        dto.setId(UUID.randomUUID());
        when(wr.findById(dto.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> wishService.patch(UUID.randomUUID(), dto));
        verify(wr).findById(dto.getId());
        verifyNoInteractions(wls, em);
    }

    @Test
    void put_whenWishlistNotExists_throwsEntityNotFoundException() {
        UUID ownerId = UUID.randomUUID();
        Wish existing = new Wish(UUID.randomUUID(), "title", null, "http://url", null, null, ownerId);
        WishDTO dto = new WishDTO();
        dto.setId(existing.getId());
        dto.setWishListId(UUID.randomUUID());

        when(wr.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(wls.findByIdOrThrow(dto.getWishListId())).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> wishService.put(ownerId, dto));
        verify(wr).findById(existing.getId());
        verify(wls).findByIdOrThrow(dto.getWishListId());
        verifyNoInteractions(em);
    }

    @Test
    void findById_passthrough() {
        UUID id = UUID.randomUUID();
        Wish wish = new Wish();
        wish.setId(id);
        when(wr.findById(id)).thenReturn(Optional.of(wish));

        Optional<Wish> res = wishService.findById(id);

        assertTrue(res.isPresent());
        assertSame(wish, res.get());
        verify(wr).findById(id);
        verifyNoInteractions(em, wls);
    }

    @Test
    void findByIdOrThrow_whenExists_returnsWish() {
        UUID id = UUID.randomUUID();
        Wish wish = new Wish();
        wish.setId(id);
        when(wr.findById(id)).thenReturn(Optional.of(wish));

        Wish res = wishService.findByIdOrThrow(id);

        assertSame(wish, res);
        verify(wr).findById(id);
        verifyNoInteractions(em, wls);
    }

    @Test
    void findByIdOrThrow_whenMissing_throwsEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(wr.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> wishService.findByIdOrThrow(id));
        verify(wr).findById(id);
        verifyNoInteractions(em, wls);
    }

    @Test
    void deleteById_passthrough() {
        UUID id = UUID.randomUUID();

        wishService.deleteById(id);

        verify(wr).deleteById(id);
        verifyNoInteractions(em, wls);
    }

    @Test
    void findAllByOwnerId_passthrough() {
        UUID ownerId = UUID.randomUUID();
        List<Wish> expected = List.of(new Wish(), new Wish());
        when(wr.findAllByOwnerId(ownerId)).thenReturn(expected);

        List<Wish> actual = wishService.findAllByOwnerId(ownerId);

        assertSame(expected, actual);
        verify(wr).findAllByOwnerId(ownerId);
        verifyNoInteractions(em, wls);
    }

    private Wishlist wishlist(UUID id) {
        Wishlist wishlist = new Wishlist();
        wishlist.setId(id);
        return wishlist;
    }
}
