package online.iwantagift.api.wishlist.util.exceptions;

import lombok.*;
import org.springframework.validation.FieldError;

@EqualsAndHashCode(callSuper = false)
//@Getter
@Setter
public class ValidationFailedException extends RuntimeException {
    private Iterable<FieldError> fieldErrors;

    public ValidationFailedException(String message) {
        super(message);
    }

    public ValidationFailedException(Iterable<FieldError> fieldErrors) {
        super();
        this.fieldErrors = fieldErrors;
    }
    public Iterable<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
