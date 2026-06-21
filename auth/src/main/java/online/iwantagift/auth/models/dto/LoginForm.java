package online.iwantagift.auth.models.dto;

import lombok.Data;

/**
 * Backing object for the login page form.
 *
 * <p>The form is submitted to Spring Security's {@code /login} processing URL, so the field names
 * must match the credentials Spring expects ({@code username} / {@code password}). The object only
 * exists so the template can bind via {@code th:object} and render authentication errors with
 * {@code th:errors} / {@code #fields}, mirroring the sign-up page.
 *
 * <p>Bad-credentials failures are not attributable to a single field, so the error is reported as a
 * global (object-level) error rather than a per-field one.
 */
@Data
public class LoginForm {
    private String username;
    private String password;
}
