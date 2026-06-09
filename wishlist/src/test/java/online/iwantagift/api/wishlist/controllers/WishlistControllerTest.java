package online.iwantagift.api.wishlist.controllers;

import online.iwantagift.api.wishlist.models.dto.WishlistDTO;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.models.mapping.WishMapper;
import online.iwantagift.api.wishlist.models.mapping.WishlistMapper;
import online.iwantagift.api.wishlist.services.WishlistService;
import online.iwantagift.api.wishlist.util.exceptions.ValidationFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistControllerTest {
    @Mock
    private WishlistService listService;
    @Mock
    private WishlistMapper wishlistMapper;
    @Mock
    private WishMapper wishMapper;
    @InjectMocks
    private WishlistController controller;

    @Test
    void getWishlist_returnsMappedDto() {
        UUID id = UUID.randomUUID();
        Wishlist wishlist = new Wishlist();
        WishlistDTO dto = new WishlistDTO();

        when(listService.findByIdOrThrow(id)).thenReturn(wishlist);
        when(wishlistMapper.toDTO(wishlist, wishMapper)).thenReturn(dto);

        WishlistDTO result = controller.getWishlist(id);

        assertSame(dto, result);
        verify(listService).findByIdOrThrow(id);
        verify(wishlistMapper).toDTO(wishlist, wishMapper);
        verifyNoMoreInteractions(listService, wishlistMapper);
    }

    @Test
    void getUserLists_isPublicAndUsesRequestedUserId() {
        UUID userId = UUID.randomUUID();
        Wishlist first = new Wishlist();
        Wishlist second = new Wishlist();
        WishlistDTO firstDto = new WishlistDTO();
        WishlistDTO secondDto = new WishlistDTO();

        when(listService.findAllByOwnerId(userId)).thenReturn(List.of(first, second));
        when(wishlistMapper.toDTO(first, wishMapper)).thenReturn(firstDto);
        when(wishlistMapper.toDTO(second, wishMapper)).thenReturn(secondDto);

        List<WishlistDTO> result = controller.getUserLists(userId);

        assertEquals(List.of(firstDto, secondDto), result);
        verify(listService).findAllByOwnerId(userId);
        verify(wishlistMapper).toDTO(first, wishMapper);
        verify(wishlistMapper).toDTO(second, wishMapper);
        verifyNoMoreInteractions(listService, wishlistMapper);
    }

    @Test
    void createWishlist_whenNoValidationErrors_usesAuthenticatedUser() {
        UUID requesterId = UUID.randomUUID();
        Authentication authentication = authentication(requesterId);
        WishlistDTO dto = new WishlistDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        Wishlist entity = new Wishlist();
        UUID id = UUID.randomUUID();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(wishlistMapper.toEntity(dto)).thenReturn(entity);
        when(listService.create(requesterId, entity)).thenReturn(id);

        Map<String, UUID> result = controller.createWishlist(dto, bindingResult, authentication);

        assertEquals(id, result.get("id"));
        verify(wishlistMapper).toEntity(dto);
        verify(listService).create(requesterId, entity);
        verifyNoMoreInteractions(wishlistMapper, listService);
    }

    @Test
    void createWishlist_whenValidationErrors_throwsValidationFailedException() {
        WishlistDTO dto = new WishlistDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("wishlistDTO", "title", "must not be blank")
        ));

        assertThrows(ValidationFailedException.class,
                () -> controller.createWishlist(dto, bindingResult, authentication(UUID.randomUUID())));

        verifyNoInteractions(wishlistMapper, listService);
    }

    @Test
    void patchWishlist_whenNoValidationErrors_callsPatchWithAuthenticatedUser() {
        UUID requesterId = UUID.randomUUID();
        WishlistDTO dto = new WishlistDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        controller.patchWishlist(dto, bindingResult, authentication(requesterId));

        verify(listService).patch(requesterId, dto);
        verifyNoMoreInteractions(listService);
        verifyNoInteractions(wishlistMapper);
    }

    @Test
    void patchWishlist_whenValidationErrors_throwsValidationFailedException() {
        WishlistDTO dto = new WishlistDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("wishlistDTO", "id", "must not be null")
        ));

        assertThrows(ValidationFailedException.class,
                () -> controller.patchWishlist(dto, bindingResult, authentication(UUID.randomUUID())));

        verifyNoInteractions(wishlistMapper, listService);
    }

    @Test
    void putWishlist_whenNoValidationErrors_callsPutWithAuthenticatedUser() {
        UUID requesterId = UUID.randomUUID();
        WishlistDTO dto = new WishlistDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        controller.putWishlist(dto, bindingResult, authentication(requesterId));

        verify(listService).put(requesterId, dto);
        verifyNoMoreInteractions(listService);
        verifyNoInteractions(wishlistMapper);
    }

    @Test
    void putWishlist_whenValidationErrors_throwsValidationFailedException() {
        WishlistDTO dto = new WishlistDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("wishlistDTO", "title", "must not be blank")
        ));

        assertThrows(ValidationFailedException.class,
                () -> controller.putWishlist(dto, bindingResult, authentication(UUID.randomUUID())));

        verifyNoInteractions(wishlistMapper, listService);
    }

    private Authentication authentication(UUID userId) {
        return new UsernamePasswordAuthenticationToken(userId.toString(), null);
    }
}
