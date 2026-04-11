package online.iwantagift.ui.web.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.models.dto.CredentialsDTO;
import online.iwantagift.ui.models.dto.TokenDTO;
import online.iwantagift.ui.models.dto.abstracts.ValidationGroups;
import online.iwantagift.ui.security.jwt.JwtCookieFactory;
import online.iwantagift.ui.services.AuthService;
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
    private final JwtCookieFactory jwtCookieFactory;
    private final AuthService authService;

    @GetMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    public String signIn() {
        return "auth/signinPage";
    }

    @GetMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    public String signUp() {
        return "auth/signupPage";
    }

    /**
     * Registers a new user and stores the returned authentication tokens in cookies.
     *
     * <p>If the submitted passwords do not match or bean validation fails, this method returns the
     * sign-up page without calling the auth service.
     *
     * @param credentials the submitted sign-up form data
     * @param bindingResult the validation result for the submitted form
     * @param response the HTTP response that receives authentication cookies
     * @return a redirect to the application root on success, or the sign-up page view on validation
     *     failure
     */
    @PostMapping(path = "/signup")
    public String signUp(@ModelAttribute @Validated(ValidationGroups.SignUp.class)
                         CredentialsDTO credentials,
                         BindingResult bindingResult,
                         HttpServletResponse response) {
        if (Objects.nonNull(credentials.getConfirmPassword()) &&
                !Objects.equals(credentials.getConfirmPassword(), credentials.getPassword())) {
            bindingResult.rejectValue("confirmPassword",
                    "validation.confirmPassword", "Passwords do not match");
        }

        if (bindingResult.hasErrors())
            return "auth/signupPage";

        TokenDTO dto = authService.signUp(credentials);

        jwtCookieFactory.createAuthCookies(dto.getJwtToken(), dto.getRefreshToken())
                .forEach(response::addCookie);

        return "redirect:/";
    }

    /**
     * Authenticates an existing user and stores the returned authentication tokens in cookies.
     *
     * <p>If bean validation fails, this method returns the sign-in page without calling the auth
     * service.
     *
     * @param credentials the submitted sign-in form data
     * @param bindingResult the validation result for the submitted form
     * @param response the HTTP response that receives authentication cookies
     * @return a redirect to the application root on success, or the sign-in page view on validation
     *     failure
     */
    @PostMapping(path = "/signin")
    @ResponseStatus(HttpStatus.OK)
    public String signIn(@ModelAttribute @Validated(ValidationGroups.SignIn.class)
                         CredentialsDTO credentials,
                         BindingResult bindingResult,
                         HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "auth/signinPage";
        }

        TokenDTO dto = authService.signIn(credentials);

        jwtCookieFactory.createAuthCookies(dto.getJwtToken(), dto.getRefreshToken())
                .forEach(response::addCookie);

        return "redirect:/";
    }

    /**
     * Clears authentication cookies and redirects the user to the application root.
     *
     * @param response the HTTP response that receives the expiring logout cookies
     * @return a redirect to the application root
     */
    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        jwtCookieFactory.createLogoutCookies()
                .forEach(response::addCookie);
        return "redirect:/";
    }

    @ModelAttribute
    public CredentialsDTO putCredentials() {
        return new CredentialsDTO();
    }

}
