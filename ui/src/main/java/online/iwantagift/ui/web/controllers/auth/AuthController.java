package online.iwantagift.ui.web.controllers.auth;

import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.models.dto.auth.CredentialsDTO;
import online.iwantagift.ui.models.validation.AuthValidationGroups;
import online.iwantagift.ui.services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * Handles browser-based authentication flows for the UI application.
 *
 * <p>Login is handled by Spring Security OAuth2. This controller only keeps the custom sign-up
 * page and redirects sign-in requests into the OAuth2 authorization flow.
 */
@RequestMapping("/auth")
@Controller
@RequiredArgsConstructor
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @GetMapping("/signin")
    public String signIn() {
        log.info("Redirecting sign-in request to OAuth2 login");
        return "redirect:/oauth2/authorization/iwag-ui";
    }

    @GetMapping("/signup")
    public String signUp() {
        log.info("Rendering sign-up page");
        return "auth/signupPage";
    }

    /**
     * Registers a new user and redirects to OAuth2 login.
     *
     * <p>If the submitted passwords do not match or bean validation fails, this method returns the
     * sign-up page without calling the auth service.
     *
     * @param credentials   the submitted sign-up form data
     * @param bindingResult the validation result for the submitted form
     * @return a redirect to the application root on success, or the sign-up page view on validation
     * failure
     */
    @PostMapping(path = "/signup")
    public String signUp(@ModelAttribute @Validated(AuthValidationGroups.SignUp.class)
                         CredentialsDTO credentials,
                         BindingResult bindingResult) {
        log.info("Handling sign-up request");

        if (Objects.nonNull(credentials.getConfirmPassword()) &&
                !Objects.equals(credentials.getConfirmPassword(), credentials.getPassword())) {
            log.info("Rejecting sign-up request due to password mismatch");
            bindingResult.rejectValue("confirmPassword",
                    "validation.confirmPassword", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            log.info("Sign-up validation failed: {} error(s)", bindingResult.getErrorCount());
            return "auth/signupPage";
        }

        try {
            authService.signUp(credentials);
        } catch (ResponseStatusException e) {
            if (!e.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT))
                throw e;
            bindingResult.rejectValue("email",
                    "validation.emailTaken", "Email already exists");
            log.info("Sign-up failed on conflict");
            return "auth/signupPage";
        }

        log.info("Sign-up succeeded, redirecting to OAuth2 login");
        return "redirect:/oauth2/authorization/iwag-ui";
    }

    @ModelAttribute
    public CredentialsDTO putCredentials() {
        return new CredentialsDTO();
    }

}
