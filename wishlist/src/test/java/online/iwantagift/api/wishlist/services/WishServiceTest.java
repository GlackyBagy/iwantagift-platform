package online.iwantagift.api.wishlist.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import online.iwantagift.api.wishlist.exceptions.AlreadyExistsException;
import online.iwantagift.api.wishlist.models.dto.WishPatchDTO;
import online.iwantagift.api.wishlist.models.dto.WishPutDTO;
import online.iwantagift.api.wishlist.models.dto.abstracts.WishWriteDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.repositories.WishRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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
    void testCreate() {
        Wish wish = new Wish();
        wish.setId(UUID.randomUUID());

        wishService.create(wish);

        verify(em, times(1)).persist(wish);
        verify(em, times(1)).flush();
        verifyNoInteractions(wr, wls);
    }

    @Test
    void testCreate_whenAlreadyExists() {
        Wish wish = new Wish();
        wish.setId(UUID.randomUUID());

        doThrow(new PersistenceException("duplicated key")).when(em).persist(wish);

        assertThrows(AlreadyExistsException.class,
                () -> wishService.create(wish));
    }

    private static Stream<? extends Arguments> patchArgs() {
        Wishlist oldWishlist = new Wishlist();
        oldWishlist.setId(UUID.randomUUID());

        Wish existingWish = new Wish(UUID.randomUUID(), "old title", "old description",
                "http://old.url", null, oldWishlist);

        Wishlist newWishlist = new Wishlist();
        newWishlist.setId(UUID.randomUUID());

        WishPatchDTO patchDto = new WishPatchDTO();
        patchDto.setId(existingWish.getId());
        patchDto.setTitle("new title");
        patchDto.setDescription("new description");
        patchDto.setUrl("http://new.url");
        patchDto.setWishListId(newWishlist.getId());

        return Stream.of(
                Arguments.of(existingWish, oldWishlist, patchDto, newWishlist));
    }

    @ParameterizedTest
    @MethodSource("patchArgs")
    void testUpdate_whenPatch_nonNull(Wish existing, Wishlist oldWishlist, WishPatchDTO patchDto, Wishlist newWishlist) {
        when(wr.findById(patchDto.getId())).thenReturn(Optional.of(existing));
        when(wls.findByIdOrThrow(newWishlist.getId())).thenReturn(newWishlist);

        wishService.update(patchDto);

        assertEquals("new title", existing.getTitle());
        assertEquals("new description", existing.getDescription());
        assertEquals("http://new.url", existing.getUrl());
        assertSame(newWishlist.getId(), existing.getWishlist().getId());

        verify(wr, times(1)).findById(patchDto.getId());
        verifyNoInteractions(em);
    }

    @ParameterizedTest
    @MethodSource("patchArgs")
    void testUpdate_whenPatch_nulls(Wish existing, Wishlist oldWishlist, WishPatchDTO patchDto, Wishlist newWishlist) {
        patchDto.setTitle(null);
        patchDto.setDescription(null);
        patchDto.setUrl(null);
        patchDto.setWishListId(null);

        when(wr.findById(patchDto.getId())).thenReturn(Optional.of(existing));

        wishService.update(patchDto);

        assertEquals("old title", existing.getTitle());
        assertEquals("old description", existing.getDescription());
        assertEquals("http://old.url", existing.getUrl());
        assertSame(oldWishlist, existing.getWishlist());

        verify(wr, times(1)).findById(patchDto.getId());
        verifyNoInteractions(wls);
    }

    private static Stream<? extends Arguments> putArgs() {
        Wishlist oldWishlist = new Wishlist();
        oldWishlist.setId(UUID.randomUUID());

        Wish existingWish = new Wish(UUID.randomUUID(), "old title", "old description",
                "http://old.url", null, oldWishlist);

        Wishlist newWishlist = new Wishlist();
        newWishlist.setId(UUID.randomUUID());

        WishPutDTO putDto = new WishPutDTO();
        putDto.setId(existingWish.getId());
        putDto.setTitle("new title");
        putDto.setDescription(null);
        putDto.setUrl("http://new.url");
        putDto.setWishListId(oldWishlist.getId());

        return Stream.of(
                Arguments.of(existingWish, oldWishlist, putDto, newWishlist));
    }

    @ParameterizedTest
    @MethodSource("patchArgs")
    void testUpdate_whenPatch_onlTitle(Wish existing, Wishlist oldWishlist, WishPatchDTO patchDto, Wishlist newWishlist) {
        patchDto.setTitle("new title");
        patchDto.setDescription(null);
        patchDto.setUrl(null);
        patchDto.setWishListId(null);

        when(wr.findById(patchDto.getId())).thenReturn(Optional.of(existing));

        wishService.update(patchDto);

        assertEquals("new title", existing.getTitle());
        assertEquals("old description", existing.getDescription());
        assertEquals("http://old.url", existing.getUrl());
        assertSame(oldWishlist, existing.getWishlist());

        verify(wr).findById(patchDto.getId());
        verifyNoInteractions(wls, em);
    }

    @ParameterizedTest
    @MethodSource("patchArgs")
    void testUpdate_whenPatch_onlDesc(Wish existing, Wishlist oldWishlist, WishPatchDTO patchDto, Wishlist newWishlist) {
        patchDto.setTitle(null);
        patchDto.setDescription("new description");
        patchDto.setUrl(null);
        patchDto.setWishListId(null);

        when(wr.findById(patchDto.getId())).thenReturn(Optional.of(existing));

        wishService.update(patchDto);

        assertEquals("old title", existing.getTitle());
        assertEquals("new description", existing.getDescription());
        assertEquals("http://old.url", existing.getUrl());
        assertSame(oldWishlist, existing.getWishlist());

        verify(wr).findById(patchDto.getId());
        verifyNoInteractions(wls, em);
    }

    @ParameterizedTest
    @MethodSource("patchArgs")
    void testUpdate_whenPatch_onlyWishListId(Wish existing, Wishlist oldWishlist, WishPatchDTO patchDto, Wishlist newWishlist) {
        patchDto.setTitle(null);
        patchDto.setDescription(null);
        patchDto.setUrl(null);
        patchDto.setWishListId(newWishlist.getId());

        when(wr.findById(patchDto.getId())).thenReturn(Optional.of(existing));
        when(wls.findByIdOrThrow(newWishlist.getId())).thenReturn(newWishlist);

        wishService.update(patchDto);

        assertEquals("old title", existing.getTitle());
        assertEquals("old description", existing.getDescription());
        assertEquals("http://old.url", existing.getUrl());
        assertEquals(newWishlist, existing.getWishlist());

        verify(wr).findById(patchDto.getId());
        verifyNoInteractions(em);
    }

    @ParameterizedTest
    @MethodSource("patchArgs")
    void testUpdate_whenPatch_onlyUrl(Wish existing, Wishlist oldWishlist, WishPatchDTO patchDto, Wishlist newWishlist) {
        patchDto.setTitle(null);
        patchDto.setDescription(null);
        patchDto.setUrl("http://new.url");
        patchDto.setWishListId(null);

        when(wr.findById(patchDto.getId())).thenReturn(Optional.of(existing));

        wishService.update(patchDto);

        assertEquals("old title", existing.getTitle());
        assertEquals("old description", existing.getDescription());
        assertEquals("http://new.url", existing.getUrl());
        assertSame(oldWishlist, existing.getWishlist());

        verify(wr).findById(patchDto.getId());
        verifyNoInteractions(wls, em);
    }

    @ParameterizedTest
    @MethodSource("putArgs")
    void testUpdate_whenPut(Wish existing, Wishlist oldWishlist, WishPutDTO putDto, Wishlist newWishlist) {
        when(wr.findById(putDto.getId())).thenReturn(Optional.of(existing));
        when(wls.findByIdOrThrow(newWishlist.getId())).thenReturn(newWishlist);
        when(wls.findByIdOrThrow(oldWishlist.getId())).thenReturn(oldWishlist);

        wishService.update(putDto);

        assertEquals("new title", existing.getTitle());
        assertNull(existing.getDescription());
        assertEquals("http://new.url", existing.getUrl());
        assertSame(oldWishlist, existing.getWishlist());

        verify(wr, times(1)).findById(putDto.getId());
        verify(wls, times(1)).findByIdOrThrow(putDto.getWishListId());
        verifyNoInteractions(em);

        putDto.setDescription("new description");
        putDto.setWishListId(newWishlist.getId());

        wishService.update(putDto);

        assertEquals("new title", existing.getTitle());
        assertEquals("new description", existing.getDescription());
        assertEquals("http://new.url", existing.getUrl());
        assertSame(newWishlist, existing.getWishlist());
    }

    @Test
    void testUpdate_whenInvalidArgs() {
        WishWriteDTO unknownDTOClassInstance = new WishWriteDTO() {
            @Override
            public String toString() {
                return super.toString();
            }
        };

        assertThrows(IllegalArgumentException.class,
                () -> wishService.update(null));
        assertThrows(IllegalArgumentException.class,
                () -> wishService.update(unknownDTOClassInstance));

        verifyNoInteractions(wr, em, wls);
    }

    @Test
    void testUpdate_whenWishNotExists() {
        when(wr.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> wishService.update(new WishPatchDTO()));
        assertThrows(EntityNotFoundException.class,
                () -> wishService.update(new WishPutDTO()));

        verify(wr, times(2)).findById(any());
        verifyNoInteractions(em, wls);
    }

    @Test
    void testUpdate_whenWishlistNotExists_forPut() {
        WishPutDTO putDto = new WishPutDTO();
        putDto.setId(UUID.randomUUID());
        putDto.setWishListId(UUID.randomUUID());

        Wish wish = new Wish();
        wish.setId(putDto.getId());

        when(wr.findById(putDto.getId())).thenReturn(Optional.of(wish));
        when(wls.findByIdOrThrow(putDto.getWishListId())).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class,
                () -> wishService.update(putDto));


        verify(wr, times(1)).findById(putDto.getId());
        verify(wls, times(1)).findByIdOrThrow(putDto.getWishListId());
        verifyNoInteractions(em);
    }

    @Test
    void testUpdate_whenWishlistNotExists_forPatch() {
        WishPatchDTO patchDto = new WishPatchDTO();
        patchDto.setId(UUID.randomUUID());
        patchDto.setWishListId(UUID.randomUUID());

        Wish wish = new Wish();
        wish.setId(patchDto.getId());

        when(wr.findById(patchDto.getId())).thenReturn(Optional.of(wish));
        when(wls.findByIdOrThrow(patchDto.getWishListId())).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class,
                () -> wishService.update(patchDto));

        verify(wr, times(1)).findById(patchDto.getId());
        verify(wls, times(1)).findByIdOrThrow(patchDto.getWishListId());
        verifyNoInteractions(em);
    }

    @Test
    void testFindById_passthrough() {
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
    void testFindByIdOrThrow_whenExists() {
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
    void testFindByIdOrThrow_whenMissing() {
        UUID id = UUID.randomUUID();

        when(wr.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> wishService.findByIdOrThrow(id));
        verify(wr).findById(id);
        verifyNoInteractions(em, wls);
    }

    @Test
    void testDeleteById_passthrough() {
        UUID id = UUID.randomUUID();

        wishService.deleteById(id);

        verify(wr).deleteById(id);
        verifyNoInteractions(em, wls);
    }
}