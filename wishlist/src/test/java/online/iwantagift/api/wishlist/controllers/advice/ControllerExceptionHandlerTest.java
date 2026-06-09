package online.iwantagift.api.wishlist.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import online.iwantagift.api.wishlist.util.exceptions.ValidationFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ControllerExceptionHandlerTest {
    @Spy
    private ControllerExceptionHandler handler;

    @Test
    void handleValidationFailedException_returnsFieldToMessageMap() {
        var fe1 = new FieldError("x", "title", "must not be blank");
        var fe2 = new FieldError("x", "url", "must be a url");
        var ex = new ValidationFailedException(List.of(fe1, fe2));

        Map<String, String> resp = invokeHandleValidation(handler, ex);

        assertEquals("must not be blank", resp.get("title"));
        assertEquals("must be a url", resp.get("url"));
    }

    @Test
    void handleEntityNotFoundException_returnsMessage() {
        var ex = new EntityNotFoundException("Entity not found");

        Map<String, String> resp = invokeHandleNotFound(handler, ex);

        assertEquals("Entity not found", resp.get("message"));
    }


    @SuppressWarnings("unchecked")
    private static Map<String, String> invokeHandleValidation(ControllerExceptionHandler h, ValidationFailedException ex) {
        try {
            var m = ControllerExceptionHandler.class.getDeclaredMethod("handleValidationFailedException", ValidationFailedException.class);
            m.setAccessible(true);
            return (Map<String, String>) m.invoke(h, ex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> invokeHandleNotFound(ControllerExceptionHandler h, EntityNotFoundException ex) {
        try {
            var m = ControllerExceptionHandler.class.getDeclaredMethod("handleEntityNotFoundException", EntityNotFoundException.class);
            m.setAccessible(true);
            return (Map<String, String>) m.invoke(h, ex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
