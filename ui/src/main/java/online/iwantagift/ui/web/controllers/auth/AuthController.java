package online.iwantagift.ui.web.controllers.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.models.dto.auth.CredentialsDTO;
import online.iwantagift.ui.models.dto.auth.TokenDTO;
import online.iwantagift.ui.models.dto.auth.abstracts.ValidationGroups;
import online.iwantagift.ui.security.jwt.JwtCookieFactory;
import online.iwantagift.ui.services.AuthService;
import online.iwantagift.ui.util.exceptions.BadRequestException;
import online.iwantagift.ui.util.exceptions.ConflictException;
import online.iwantagift.ui.util.exceptions.ServiceUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Handles browser-based authentication flows for the UI application.
 *
 * <p>This controller renders sign-in and sign-up pages, submits credentials to the auth service,
 * stores returned tokens in HTTP cookies, and clears those cookies on logout.
 */
@RequestMapping("/auth")
@Controller
@RequiredArgsConstructor
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtCookieFactory jwtCookieFactory;
    private final AuthService authService;

    @GetMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    public String signIn() {
        log.info("Rendering sign-in page");
        return "auth/signinPage";
    }

    @GetMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    public String signUp() {
        log.info("Rendering sign-up page");
        return "auth/signupPage";
    }

    /**
     * Registers a new user and stores the returned authentication tokens in cookies.
     *
     * <p>If the submitted passwords do not match or bean validation fails, this method returns the
     * sign-up page without calling the auth service.
     *
     * @param credentials   the submitted sign-up form data
     * @param bindingResult the validation result for the submitted form
     * @param response      the HTTP response that receives authentication cookies
     * @return a redirect to the application root on success, or the sign-up page view on validation
     * failure
     */
    @PostMapping(path = "/signup")
    public String signUp(@ModelAttribute @Validated(ValidationGroups.SignUp.class)
                         CredentialsDTO credentials,
                         BindingResult bindingResult,
                         HttpServletResponse response) {
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

        TokenDTO dto;

        try {
            dto = authService.signUp(credentials);
        } catch (ConflictException e) {
            bindingResult.rejectValue("email",
                    "validation.emailTaken", "Email already exists");
            log.info("Sign-up failed on conflict");
            return "auth/signupPage";
        }

        jwtCookieFactory.createAuthCookies(dto.getJwtToken(), dto.getRefreshToken())
                .forEach(response::addCookie);

        log.info("Sign-up succeeded, authentication cookies were set");
        return "redirect:/";
    }

    /**
     * Authenticates an existing user and stores the returned authentication tokens in cookies.
     *
     * <p>If bean validation fails, this method returns the sign-in page without calling the auth
     * service.
     *
     * @param credentials   the submitted sign-in form data
     * @param bindingResult the validation result for the submitted form
     * @param response      the HTTP response that receives authentication cookies
     * @return a redirect to the application root on success, or the sign-in page view on validation
     * failure
     */
    @PostMapping(path = "/signin")
    public String signIn(@ModelAttribute @Validated(ValidationGroups.SignIn.class)
                         CredentialsDTO credentials,
                         BindingResult bindingResult,
                         HttpServletResponse response) {
        log.info("Handling sign-in request");

        if (bindingResult.hasErrors()) {
            log.info("Sign-in validation failed: {} error(s)", bindingResult.getErrorCount());
            return "auth/signinPage";
        }
        TokenDTO dto;

        try {
            dto = authService.signIn(credentials);
        } catch (BadRequestException e) {
            bindingResult.reject("invalidCredentials", "Wrong password or email");
            log.info("Sign-in failed: {}", e.getMessage());
            return "auth/signinPage";
        }

        jwtCookieFactory.createAuthCookies(dto.getJwtToken(), dto.getRefreshToken())
                .forEach(response::addCookie);

        log.info("Sign-in succeeded, authentication cookies were set");
        return "redirect:/";
    }

    /**
     * Clears authentication cookies and redirects the user to the application root.
     *
     * @param response the HTTP response that receives the expiring logout cookies
     * @return a redirect to the application root
     */
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        log.info("Handling logout request");
        jwtCookieFactory.createLogoutCookies()
                .forEach(response::addCookie);
        log.info("Logout cookies were set");
        return "redirect:/";
    }

    @ModelAttribute
    public CredentialsDTO putCredentials() {
        return new CredentialsDTO();
    }

    @ExceptionHandler(ServiceUnauthorizedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected String handleUnauthorizedException(ServiceUnauthorizedException e) {
        log.error("Service unauthorized", e);
        return "error/500";
    }

}
