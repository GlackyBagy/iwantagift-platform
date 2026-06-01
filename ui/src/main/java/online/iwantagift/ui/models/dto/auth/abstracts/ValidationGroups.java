package online.iwantagift.ui.models.dto.auth.abstracts;

import jakarta.validation.groups.Default;

public final class ValidationGroups {
    public interface SignUp extends SignIn {
    }

    public interface SignIn extends Default {
    }
}
