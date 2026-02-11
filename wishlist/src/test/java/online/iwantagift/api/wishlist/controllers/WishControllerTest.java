package online.iwantagift.api.wishlist.controllers;

import jakarta.persistence.EntityNotFoundException;
import online.iwantagift.api.wishlist.exceptions.ValidationFailedException;
import online.iwantagift.api.wishlist.models.dto.WishCreateDTO;
import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.dto.WishPatchDTO;
import online.iwantagift.api.wishlist.models.dto.WishPutDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.mapping.WishMapper;
import online.iwantagift.api.wishlist.services.WishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private WishMapper wishMapper;
    @InjectMocks
    private WishController controller;

    @Test
    void createWish_whenNoValidationErrors_returnsIdAndCallsService() {
        var dto = mock(WishCreateDTO.class);
        var bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        var wishEntity = new Wish();
        when(wishMapper.toEntity(dto)).thenReturn(wishEntity);

        var id = UUID.randomUUID();
        when(wishService.create(wishEntity)).thenReturn(id);

        Map<String, UUID> resp = controller.createWish(dto, bindingResult);

        assertEquals(id, resp.get("id"));
        verify(wishMapper).toEntity(dto);
        verify(wishService).create(wishEntity);
        verifyNoMoreInteractions(wishMapper, wishService);
    }

    @Test
    void createWish_whenValidationErrors_throwsValidationFailedException() {
        var dto = mock(WishCreateDTO.class);
        var bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(true);
        var fe = new FieldError("wishCreateDTO", "title", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fe));

        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> controller.createWish(dto, bindingResult)
        );

        verifyNoInteractions(wishMapper, wishService);
        assertNotNull(ex.getFieldErrors());
    }

    @Test
    void getWish_returnsMappedDto() {
        UUID id = UUID.randomUUID();
        Wish wish = new Wish();
        WishDTO wishDTO = mock(WishDTO.class);

        when(wishService.findByIdOrThrow(id)).thenReturn(wish);
        when(wishMapper.toDTO(wish)).thenReturn(wishDTO);

        WishDTO result = controller.getWish(id);

        assertSame(wishDTO, result);
        verify(wishService).findByIdOrThrow(id);
        verify(wishMapper).toDTO(wish);
        verifyNoMoreInteractions(wishService, wishMapper);
    }

    @Test
    void patchWish_whenNoValidationErrors_callsUpdate() {
        var dto = mock(WishPatchDTO.class);
        var bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        controller.patchWish(dto, bindingResult);

        verify(wishService).update(dto);
        verifyNoMoreInteractions(wishService);
        verifyNoInteractions(wishMapper);
    }

    @Test
    void patchWish_whenValidationErrors_throwsValidationFailedException() {
        var dto = mock(WishPatchDTO.class);
        var bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("wishPatchDTO", "id", "must not be null")
        ));

        assertThrows(ValidationFailedException.class, () -> controller.patchWish(dto, bindingResult));

        verifyNoInteractions(wishService, wishMapper);
    }

    @Test
    void putWish_whenNoValidationErrors_callsUpdate() {
        var dto = mock(WishPutDTO.class);
        var bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        controller.putWish(dto, bindingResult);

        verify(wishService).update(dto);
        verifyNoMoreInteractions(wishService);
        verifyNoInteractions(wishMapper);
    }

    @Test
    void putWish_whenValidationErrors_throwsValidationFailedException() {
        var dto = mock(WishPutDTO.class);
        var bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("wishPutDTO", "title", "size must be between 1 and 255")
        ));

        assertThrows(ValidationFailedException.class, () -> controller.putWish(dto, bindingResult));

        verifyNoInteractions(wishService, wishMapper);
    }

    @Test
    void handleValidationFailedException_returnsFieldToMessageMap() {
        var fe1 = new FieldError("x", "title", "must not be blank");
        var fe2 = new FieldError("x", "url", "must be a url");
        var ex = new ValidationFailedException(List.of(fe1, fe2));

        Map<String, String> resp = invokeHandleValidation(controller, ex);

        assertEquals("must not be blank", resp.get("title"));
        assertEquals("must be a url", resp.get("url"));
    }

    @Test
    void handleEntityNotFoundException_returnsMessage() {
        var ex = new EntityNotFoundException("Wish not found");

        Map<String, String> resp = invokeHandleNotFound(controller, ex);

        assertEquals("Wish not found", resp.get("message"));
    }


    @SuppressWarnings("unchecked")
    private static Map<String, String> invokeHandleValidation(WishController c, ValidationFailedException ex) {
        try {
            var m = WishController.class.getDeclaredMethod("handleValidationFailedException", ValidationFailedException.class);
            m.setAccessible(true);
            return (Map<String, String>) m.invoke(c, ex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> invokeHandleNotFound(WishController c, EntityNotFoundException ex) {
        try {
            var m = WishController.class.getDeclaredMethod("handleEntityNotFoundException", EntityNotFoundException.class);
            m.setAccessible(true);
            return (Map<String, String>) m.invoke(c, ex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
