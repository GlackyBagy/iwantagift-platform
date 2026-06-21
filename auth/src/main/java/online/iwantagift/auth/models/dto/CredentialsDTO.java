package online.iwantagift.auth.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;
import online.iwantagift.auth.models.dto.abstracts.ValidationGroups.*;

/**
 * Form payload for UI authentication requests.
 *
 * <p>This DTO is shared by sign-in, sign-up and credentials-change flows. The {@code nickname} and
 * {@code confirmPassword} fields are required only for the {@link SignUp} validation group, while
 * {@code email} and {@code password} are required in both sign-up and sign-in flows.
 *
 * <p>There are 2 credentials-change flows:</p>
 * <b>Change-password</b> requires {@code password} and {@code confirmPassword};
 * other fields must be {@code null}.
 * <br>
 * <b>Change-email</b> requires a valid {@code email}; other fields must be {@code null}.
 *
 */
@Data
public class CredentialsDTO {
    @NotBlank(message = "Username must not be blank", groups = SignUp.class)
    @Size(min = 1, max = 64, groups = SignUp.class,
            message = "Username must be at least 1 char, and not longer than 64")
    @Null(groups = {ChangeEmail.class, ChangePassword.class})
    private String nickname;

    @NotBlank(message = "Email must not be blank", groups = {Default.class, ChangeEmail.class})
    @Email(message = "Invalid email format", groups = {Default.class, ChangeEmail.class})
    @Null(groups = ChangePassword.class)
    private String email;

    @Size(min = 8, max = 128,
            message = "Password must be at least 8 chars, and not longer than 128",
            groups = {Default.class, ChangePassword.class})
    @NotBlank(message = "Password must not be blank",
            groups = {Default.class, ChangePassword.class})
    @Null(groups = {ChangeEmail.class})
    private String password;

    @NotBlank(groups = {SignUp.class, ChangePassword.class},
            message = "You should confirm your password")
    @Null(groups = {ChangeEmail.class})
    private String confirmPassword;
}
