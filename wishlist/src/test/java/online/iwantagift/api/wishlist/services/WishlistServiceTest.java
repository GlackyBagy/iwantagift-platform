package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import online.iwantagift.api.wishlist.models.dto.WishlistDTO;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.repositories.WishlistRepository;
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
class WishlistServiceTest {
    @Mock
    private EntityManager em;
    @Mock
    private WishlistRepository lr;
    @InjectMocks
    WishlistService service;

    @Test
    void create_setsOwnerIdClearsIdAndPersistsWishlist() {
        UUID requesterId = UUID.randomUUID();
        Wishlist wishlist = new Wishlist();
        wishlist.setId(UUID.randomUUID());

        doAnswer(invocation -> {
            wishlist.setId(UUID.randomUUID());
            return null;
        }).when(em).flush();

        UUID id = service.create(requesterId, wishlist);

        assertNotNull(id);
        assertEquals(requesterId, wishlist.getOwnerId());
        verify(em).persist(wishlist);
        verify(em).flush();
        verifyNoInteractions(lr);
    }

    @Test
    void create_whenAlreadyExists_throwsAlreadyExistsException() {
        UUID requesterId = UUID.randomUUID();
        Wishlist wishlist = new Wishlist();
        wishlist.setId(UUID.randomUUID());

        doThrow(new PersistenceException("duplicated key")).when(em).persist(wishlist);

        assertThrows(AlreadyExistsException.class, () -> service.create(requesterId, wishlist));
        verify(em).persist(wishlist);
        verifyNoInteractions(lr);
    }

    @Test
    void patch_whenRequesterOwnsWishlist_updatesOnlyNonNullFields() {
        UUID ownerId = UUID.randomUUID();
        Wishlist existing = new Wishlist(UUID.randomUUID(), "old title", "old desc", null, ownerId, null);
        WishlistDTO dto = new WishlistDTO();
        dto.setId(existing.getId());
        dto.setTitle("new title");
        dto.setDescription("new description");

        when(lr.findById(existing.getId())).thenReturn(Optional.of(existing));

        service.patch(ownerId, dto);

        assertEquals("new title", existing.getTitle());
        assertEquals("new description", existing.getDescription());
        verify(lr).findById(existing.getId());
        verifyNoInteractions(em);
    }

    @Test
    void patch_whenDtoFieldsAreNull_preservesExistingValues() {
        UUID ownerId = UUID.randomUUID();
        Wishlist existing = new Wishlist(UUID.randomUUID(), "old title", "old desc", null, ownerId, null);
        WishlistDTO dto = new WishlistDTO();
        dto.setId(existing.getId());

        when(lr.findById(existing.getId())).thenReturn(Optional.of(existing));

        service.patch(ownerId, dto);

        assertEquals("old title", existing.getTitle());
        assertEquals("old desc", existing.getDescription());
        verify(lr).findById(existing.getId());
        verifyNoInteractions(em);
    }

    @Test
    void patch_whenRequesterDoesNotOwnWishlist_throwsUnauthorizedException() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Wishlist existing = new Wishlist(UUID.randomUUID(), "title", "desc", null, ownerId, null);
        WishlistDTO dto = new WishlistDTO();
        dto.setId(existing.getId());
        dto.setTitle("new title");

        when(lr.findById(existing.getId())).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () -> service.patch(requesterId, dto));
        verify(lr).findById(existing.getId());
        verifyNoInteractions(em);
    }

    @Test
    void put_whenRequesterOwnsWishlist_replacesWritableFields() {
        UUID ownerId = UUID.randomUUID();
        Wishlist existing = new Wishlist(UUID.randomUUID(), "old title", "old desc", null, ownerId, null);
        WishlistDTO dto = new WishlistDTO();
        dto.setId(existing.getId());
        dto.setTitle("new title");
        dto.setDescription(null);

        when(lr.findById(existing.getId())).thenReturn(Optional.of(existing));

        service.put(ownerId, dto);

        assertEquals("new title", existing.getTitle());
        assertNull(existing.getDescription());
        verify(lr).findById(existing.getId());
        verifyNoInteractions(em);
    }

    @Test
    void put_whenRequesterDoesNotOwnWishlist_throwsUnauthorizedException() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Wishlist existing = new Wishlist(UUID.randomUUID(), "title", "desc", null, ownerId, null);
        WishlistDTO dto = new WishlistDTO();
        dto.setId(existing.getId());
        dto.setTitle("new title");

        when(lr.findById(existing.getId())).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () -> service.put(requesterId, dto));
        verify(lr).findById(existing.getId());
        verifyNoInteractions(em);
    }

    @Test
    void patch_whenNotExists_throwsEntityNotFoundException() {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(UUID.randomUUID());
        when(lr.findById(dto.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.patch(UUID.randomUUID(), dto));
        verify(lr).findById(dto.getId());
        verifyNoInteractions(em);
    }

    @Test
    void put_whenNotExists_throwsEntityNotFoundException() {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(UUID.randomUUID());
        when(lr.findById(dto.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.put(UUID.randomUUID(), dto));
        verify(lr).findById(dto.getId());
        verifyNoInteractions(em);
    }

    @Test
    void findByIdOrThrow_whenExists_returnsWishlist() {
        UUID id = UUID.randomUUID();
        Wishlist wl = new Wishlist(id, "t", "d", null, null, null);
        when(lr.findById(id)).thenReturn(Optional.of(wl));

        Wishlist found = service.findByIdOrThrow(id);

        assertSame(wl, found);
        verify(lr).findById(id);
        verifyNoInteractions(em);
    }

    @Test
    void findByIdOrThrow_whenMissing_throwsEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(lr.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findByIdOrThrow(id));
        verify(lr).findById(id);
        verifyNoInteractions(em);
    }

    @Test
    void findById_passthrough() {
        UUID id = UUID.randomUUID();
        Wishlist wl = new Wishlist(id, "t", "d", null, null, null);
        when(lr.findById(id)).thenReturn(Optional.of(wl));

        Optional<Wishlist> res = service.findById(id);

        assertTrue(res.isPresent());
        assertSame(wl, res.get());
        verify(lr).findById(id);
        verifyNoInteractions(em);
    }

    @Test
    void createDefaultList_whenExists_returnsExistingWishlist() {
        UUID ownerId = UUID.randomUUID();
        Wishlist existing = new Wishlist(UUID.randomUUID(), "DEFAULT_WISHLIST", "Default wishlist", null, ownerId, null);
        when(lr.findByOwnerIdAndTitle(ownerId, "DEFAULT_WISHLIST")).thenReturn(Optional.of(existing));

        Wishlist result = service.createDefaultList(ownerId);

        assertSame(existing, result);
        verify(lr).findByOwnerIdAndTitle(ownerId, "DEFAULT_WISHLIST");
        verifyNoMoreInteractions(lr);
        verifyNoInteractions(em);
    }

    @Test
    void createDefaultList_whenMissing_savesDefaultWishlist() {
        UUID ownerId = UUID.randomUUID();
        when(lr.findByOwnerIdAndTitle(ownerId, "DEFAULT_WISHLIST")).thenReturn(Optional.empty());
        when(lr.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wishlist result = service.createDefaultList(ownerId);

        assertEquals(ownerId, result.getOwnerId());
        assertEquals("DEFAULT_WISHLIST", result.getTitle());
        assertEquals("Default wishlist", result.getDescription());
        verify(lr).findByOwnerIdAndTitle(ownerId, "DEFAULT_WISHLIST");
        verify(lr).save(result);
        verifyNoInteractions(em);
    }

    @Test
    void deleteById_passthrough() {
        UUID id = UUID.randomUUID();

        service.deleteById(id);

        verify(lr).deleteById(id);
        verifyNoInteractions(em);
    }

    @Test
    void findAllByOwnerId_passthrough() {
        UUID ownerId = UUID.randomUUID();
        List<Wishlist> expected = List.of(
                new Wishlist(UUID.randomUUID(), "t1", "d1", null, ownerId, null),
                new Wishlist(UUID.randomUUID(), "t2", "d2", null, ownerId, null)
        );
        when(lr.findAllByOwnerId(ownerId)).thenReturn(expected);

        List<Wishlist> actual = service.findAllByOwnerId(ownerId);

        assertEquals(expected, actual);
        verify(lr).findAllByOwnerId(ownerId);
        verifyNoInteractions(em);
    }
}
