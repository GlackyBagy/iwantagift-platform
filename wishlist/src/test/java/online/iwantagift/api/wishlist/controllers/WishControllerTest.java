package online.iwantagift.api.wishlist.controllers;

import online.iwantagift.api.wishlist.util.exceptions.ValidationFailedException;
import online.iwantagift.api.wishlist.messaging.kafka.NewWishProducer;
import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.models.events.WishCreatedEvent;
import online.iwantagift.api.wishlist.models.mapping.WishMapper;
import online.iwantagift.api.wishlist.services.WishService;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class WishControllerTest {
    @Mock
    private WishService wishService;
    @Mock
    private WishlistService wlService;
    @Mock
    private WishMapper wishMapper;
    @Mock
    private NewWishProducer wishProducer;
    @InjectMocks
    private WishController controller;

    @Test
    void createWish_whenNoValidationErrors_usesAuthenticatedUserAndSendsKafka() {
        UUID requesterId = UUID.randomUUID();
        UUID wishlistId = UUID.randomUUID();
        Authentication authentication = authentication(requesterId);
        WishDTO dto = new WishDTO();
        dto.setTitle("PS5");
        dto.setDescription("Slim");
        dto.setUrl("https://example.com/ps5");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        Wish wishEntity = new Wish();
        Wishlist wishlist = new Wishlist();
        wishlist.setId(wishlistId);
        wishEntity.setWishlist(wishlist);
        when(wishMapper.toEntity(dto, requesterId, wlService)).thenReturn(wishEntity);

        UUID id = UUID.randomUUID();
        when(wishService.create(requesterId, wishEntity)).thenReturn(id);

        Map<String, UUID> resp = controller.createWish(dto, bindingResult, authentication);

        assertEquals(id, resp.get("id"));
        assertNull(dto.getOwnerId());
        verify(wishMapper).toEntity(dto, requesterId, wlService);
        verify(wishService).create(requesterId, wishEntity);
        ArgumentCaptor<WishCreatedEvent> eventCaptor = ArgumentCaptor.forClass(WishCreatedEvent.class);
        verify(wishProducer).send(eventCaptor.capture());
        WishCreatedEvent event = eventCaptor.getValue();
        assertEquals(id, event.wishId());
        assertEquals(requesterId, event.ownerId());
        assertEquals("PS5", event.title());
        assertEquals("Slim", event.description());
        assertEquals("https://example.com/ps5", event.url());
        assertEquals(wishlistId, event.wishListId());
        verifyNoMoreInteractions(wishMapper, wishService, wishProducer);
    }

    @Test
    void createWish_whenValidationErrors_throwsValidationFailedException() {
        WishDTO dto = new WishDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("wishDTO", "title", "must not be blank")
        ));

        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> controller.createWish(dto, bindingResult, authentication(UUID.randomUUID()))
        );

        assertNotNull(ex.getFieldErrors());
        verifyNoInteractions(wishMapper, wishService, wishProducer);
    }

    @Test
    void getWish_returnsMappedDto() {
        UUID id = UUID.randomUUID();
        Wish wish = new Wish();
        WishDTO wishDTO = new WishDTO();

        when(wishService.findByIdOrThrow(id)).thenReturn(wish);
        when(wishMapper.toDTO(wish)).thenReturn(wishDTO);

        WishDTO result = controller.getWish(id);

        assertSame(wishDTO, result);
        verify(wishService).findByIdOrThrow(id);
        verify(wishMapper).toDTO(wish);
        verifyNoMoreInteractions(wishService, wishMapper);
        verifyNoInteractions(wishProducer);
    }

    @Test
    void patchWish_whenNoValidationErrors_callsPatchWithAuthenticatedUser() {
        UUID requesterId = UUID.randomUUID();
        WishDTO dto = new WishDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        controller.patchWish(dto, bindingResult, authentication(requesterId));

        verify(wishService).patch(requesterId, dto);
        verifyNoMoreInteractions(wishService);
        verifyNoInteractions(wishMapper, wishProducer);
    }

    @Test
    void patchWish_whenValidationErrors_throwsValidationFailedException() {
        WishDTO dto = new WishDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("wishDTO", "id", "must not be null")
        ));

        assertThrows(ValidationFailedException.class,
                () -> controller.patchWish(dto, bindingResult, authentication(UUID.randomUUID())));

        verifyNoInteractions(wishService, wishMapper, wishProducer);
    }

    @Test
    void putWish_whenNoValidationErrors_callsPutWithAuthenticatedUser() {
        UUID requesterId = UUID.randomUUID();
        WishDTO dto = new WishDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        controller.putWish(dto, bindingResult, authentication(requesterId));

        verify(wishService).put(requesterId, dto);
        verifyNoMoreInteractions(wishService);
        verifyNoInteractions(wishMapper, wishProducer);
    }

    @Test
    void putWish_whenValidationErrors_throwsValidationFailedException() {
        WishDTO dto = new WishDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("wishDTO", "title", "must not be blank")
        ));

        assertThrows(ValidationFailedException.class,
                () -> controller.putWish(dto, bindingResult, authentication(UUID.randomUUID())));

        verifyNoInteractions(wishService, wishMapper, wishProducer);
    }

    private Authentication authentication(UUID userId) {
        return new UsernamePasswordAuthenticationToken(userId.toString(), null);
    }
}
