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
