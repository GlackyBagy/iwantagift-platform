package online.iwantagift.api.wishlist.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.api.wishlist.exceptions.ValidationFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "online.iwantagift.api.wishlist.controllers")
@Slf4j
public class ControllerExceptionHandler {
    @ExceptionHandler(ValidationFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private Map<String, String> handleValidationFailedException(ValidationFailedException e) {
        Iterable<FieldError> errors = e.getFieldErrors();
        Map<String, String> errorMap = new HashMap<>();
        for (FieldError error : errors)
            errorMap.put(error.getField(), error.getDefaultMessage());

        return errorMap;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private Map<String, String> handleEntityNotFoundException(EntityNotFoundException e) {
        return Collections.singletonMap("message", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private void handleException(Exception e) {
        log.error(e.getMessage(), e);
    }
}
