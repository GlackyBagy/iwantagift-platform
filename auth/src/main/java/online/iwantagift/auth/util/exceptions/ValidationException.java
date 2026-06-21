package online.iwantagift.auth.util.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

@Getter
public class ValidationException extends RuntimeException {
    private BindingResult result;

    public ValidationException() {
        super();
    }

    public ValidationException(BindingResult result) {
        super();
        this.result = result;
    }
}
