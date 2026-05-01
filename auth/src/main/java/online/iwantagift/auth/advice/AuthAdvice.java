package online.iwantagift.auth.advice;

import lombok.extern.slf4j.Slf4j;
import online.iwantagift.auth.controllers.AuthController;
import online.iwantagift.auth.util.exceptions.EmailAlreadyExistsException;
import online.iwantagift.auth.util.exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackageClasses = AuthController.class)
@Slf4j
public class AuthAdvice {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private Map<String, String> handleValidationException(ValidationException exception) {
        Map<String, String> res = new HashMap<>();

        for (ObjectError error : exception.getResult().getAllErrors()) {
            if (error instanceof FieldError fieldError)
                res.put(fieldError.getField(), error.getDefaultMessage());
            else
                res.put(error.getObjectName(), error.getDefaultMessage());
        }

        return res;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    private Map<String, String> handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {
        return Map.of("email", "Account with provided email already exists");
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private Map<String, String> handleBadCredentials(BadCredentialsException exception) {
        return Map.of("credentials", "Bad credentials");
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private void handleIllegalStateException(IllegalStateException exception) {
        log.error(exception.getMessage(), exception);
    }
}
