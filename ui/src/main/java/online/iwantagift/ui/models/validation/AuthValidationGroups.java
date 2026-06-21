package online.iwantagift.ui.models.validation;

import jakarta.validation.groups.Default;

public final class AuthValidationGroups {
    public interface SignUp extends SignIn {
    }

    public interface SignIn extends Default {
    }

    public interface ChangeEmail {
    }

    public interface ChangePassword {
    }

    public interface ConfirmEmail {
    }
}
