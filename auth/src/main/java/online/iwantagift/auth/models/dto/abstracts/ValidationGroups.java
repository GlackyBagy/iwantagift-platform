package online.iwantagift.auth.models.dto.abstracts;

import jakarta.validation.groups.Default;

public final class ValidationGroups {
    public interface SignUp extends SignIn {
    }

    public interface SignIn extends Default {
    }

    public interface ChangeEmail {
    }

    public interface ChangePassword {
    }
}
