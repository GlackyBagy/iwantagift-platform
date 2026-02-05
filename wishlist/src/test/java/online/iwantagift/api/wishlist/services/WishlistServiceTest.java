package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import online.iwantagift.api.wishlist.exceptions.AlreadyExistsException;
import online.iwantagift.api.wishlist.models.dto.abstracts.WishlistWriteDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistPatchDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistPutDTO;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.repositories.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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
    void testCreate() {
        Wishlist wishlist = new Wishlist();
        wishlist.setId(UUID.randomUUID());

        service.create(wishlist);

        verify(em, times(1)).persist(wishlist);
        verify(em, times(1)).flush();
        verifyNoInteractions(lr);
    }

    @Test
    void testCreate_whenAlreadyExists() {
        Wishlist wishlist = new Wishlist();
        wishlist.setId(UUID.randomUUID());

        doThrow(new PersistenceException("duplicated key")).when(em).persist(wishlist);

        assertThrows(AlreadyExistsException.class,
                () -> service.create(wishlist));

        verify(em, times(1)).persist(wishlist);
        verifyNoInteractions(lr);
    }

    @Test
    void testUpdate_whenPut() {
        Wishlist existing = new Wishlist(UUID.randomUUID(), "old title", "old desc", null, null, null);

        WishlistPutDTO dto = new WishlistPutDTO();
        dto.setId(existing.getId());
        dto.setTitle("new title");
        dto.setDescription(null);

        when(lr.findById(existing.getId())).thenReturn(Optional.of(existing));

        service.update(dto);

        assertEquals("new title", existing.getTitle());
        assertNull(existing.getDescription());

        verify(lr, times(1)).findById(existing.getId());
        verifyNoInteractions(em);

        dto.setTitle("super new title");
        dto.setDescription("new description");

        service.update(dto);

        assertEquals("super new title", existing.getTitle());
        assertEquals("new description", existing.getDescription());
    }

    static Stream<? extends Arguments> patchArgs() {
        Wishlist existing = new Wishlist(UUID.randomUUID(), "old title", "old description", null, null, null);
        WishlistPatchDTO dto = new WishlistPatchDTO();
        dto.setId(existing.getId());
        dto.setTitle("new title");
        dto.setDescription("new description");

        return Stream.of(Arguments.of(
                existing, dto
        ));
    }

    @ParameterizedTest
    @MethodSource("patchArgs")
    void testUpdate_whenPatch_nonNull(Wishlist existing, WishlistPatchDTO dto) {
        when(lr.findById(existing.getId())).thenReturn(Optional.of(existing));

        service.update(dto);

        assertEquals("new title", existing.getTitle());
        assertEquals("new description", existing.getDescription());

        verify(lr, times(1)).findById(existing.getId());
        verifyNoInteractions(em);
    }

    @ParameterizedTest
    @MethodSource("patchArgs")
    void testUpdate_whenPatch_Nulls(Wishlist existing, WishlistPatchDTO dto) {
        dto.setTitle(null);
        dto.setDescription(null);

        when(lr.findById(existing.getId())).thenReturn(Optional.of(existing));

        service.update(dto);

        assertEquals("old title", existing.getTitle());
        assertEquals("old description", existing.getDescription());

        verify(lr, times(1)).findById(existing.getId());
        verifyNoInteractions(em);
    }

    @ParameterizedTest
    @MethodSource("patchArgs")
    void testUpdate_whenPatch_onlyTitle(Wishlist existing, WishlistPatchDTO dto) {
        dto.setDescription(null);

        when(lr.findById(existing.getId())).thenReturn(Optional.of(existing));

        service.update(dto);

        assertEquals("new title", existing.getTitle());
        assertEquals("old description", existing.getDescription());

        verify(lr, times(1)).findById(existing.getId());
        verifyNoInteractions(em);
    }

    @ParameterizedTest
    @MethodSource("patchArgs")
    void testUpdate_whenPatch_onlyDescription(Wishlist existing, WishlistPatchDTO dto) {
        dto.setTitle(null);

        when(lr.findById(existing.getId())).thenReturn(Optional.of(existing));

        service.update(dto);

        assertEquals("old title", existing.getTitle());
        assertEquals("new description", existing.getDescription());

        verify(lr, times(1)).findById(existing.getId());
        verifyNoInteractions(em);
    }

    @Test
    void testUpdate_whenInvalidArgs() {
        WishlistWriteDTO unknownDTOClassInstance = new WishlistWriteDTO() {
            @Override
            public String toString() {
                return super.toString();
            }
        };

        assertThrows(IllegalArgumentException.class,
                () -> service.update(null));
        assertThrows(IllegalArgumentException.class,
                () -> service.update(unknownDTOClassInstance));

        verifyNoInteractions(em);
    }

    @Test
    void testUpdate_whenNotExists() {
        WishlistPutDTO putDTO = new WishlistPutDTO();
        WishlistPatchDTO patchDTO = new WishlistPatchDTO();
        putDTO.setId(UUID.randomUUID());
        patchDTO.setId(putDTO.getId());

        when(lr.findById(putDTO.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.update(putDTO));
        assertThrows(EntityNotFoundException.class,
                () -> service.update(patchDTO));

        verify(lr, times(2)).findById(putDTO.getId());
        verifyNoInteractions(em);
    }

    @Test
    void testFindByIdOrThrow_whenExists() {
        UUID id = UUID.randomUUID();
        Wishlist wl = new Wishlist(id, "t", "d", null, null, null);

        when(lr.findById(id)).thenReturn(Optional.of(wl));

        Wishlist found = service.findByIdOrThrow(id);

        assertSame(wl, found);
        verify(lr, times(1)).findById(id);
        verifyNoInteractions(em);
    }

    @Test
    void testFindByIdOrThrow_whenMissing() {
        UUID id = UUID.randomUUID();

        when(lr.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.findByIdOrThrow(id));

        verify(lr, times(1)).findById(id);
        verifyNoInteractions(em);
    }

    @Test
    void testFindById_passthrough() {
        UUID id = UUID.randomUUID();
        Wishlist wl = new Wishlist(id, "t", "d", null, null, null);

        when(lr.findById(id)).thenReturn(Optional.of(wl));

        Optional<Wishlist> res = service.findById(id);

        assertTrue(res.isPresent());
        assertSame(wl, res.get());
        verify(lr, times(1)).findById(id);
        verifyNoInteractions(em);
    }

    @Test
    void testDeleteById_passthrough() {
        UUID id = UUID.randomUUID();

        service.deleteById(id);

        verify(lr, times(1)).deleteById(id);
        verifyNoInteractions(em);
    }

    @Test
    void testFindAllByOwnerId_passthrough() {
        UUID ownerId = UUID.randomUUID();
        List<Wishlist> expected = List.of(
                new Wishlist(UUID.randomUUID(), "t1", "d1", null, ownerId, null),
                new Wishlist(UUID.randomUUID(), "t2", "d2", null, ownerId, null)
        );

        when(lr.findAllByOwnerId(ownerId)).thenReturn(expected);

        List<Wishlist> actual = service.findAllByOwnerId(ownerId);

        assertEquals(expected, actual);
        verify(lr, times(1)).findAllByOwnerId(ownerId);
        verifyNoInteractions(em);
    }
}
