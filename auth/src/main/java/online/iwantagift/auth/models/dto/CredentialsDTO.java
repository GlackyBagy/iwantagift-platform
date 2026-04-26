package online.iwantagift.auth.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import online.iwantagift.auth.models.dto.abstracts.ValidationGroups;

/**
 * Form payload for UI authentication requests.
 *
 * <p>This DTO is shared by both sign-in and sign-up flows. The {@code nickname} and
 * {@code confirmPassword} fields are required only for the {@link ValidationGroups.SignUp} validation group, while
 * {@code email} and {@code password} are required in both flows.
 */
@Data
public class CredentialsDTO {
    @NotBlank(message = "Username must not be blank", groups = ValidationGroups.SignUp.class)
    @Size(min = 1, max = 64, groups = ValidationGroups.SignUp.class,
            message = "Username must be at least 1 char, and not longer than 64")
    private String nickname;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, max = 128,
            message = "Password must be at least 8 chars, and not longer than 128")
    @NotBlank(message = "Password must not be blank")
    private String password;

    @NotBlank(groups = ValidationGroups.SignUp.class,
            message = "You should confirm your password")
    private String confirmPassword;
}
