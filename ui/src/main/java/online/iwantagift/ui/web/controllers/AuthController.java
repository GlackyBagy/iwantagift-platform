package online.iwantagift.ui.web.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.models.dto.CredentialsDTO;
import online.iwantagift.ui.models.dto.abstracts.ValidationGroups;
import online.iwantagift.ui.security.jwt.JwtCookieFactory;
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
                         BindingResult bindingResult) {
        if (Objects.nonNull(credentials.getConfirmPassword()) &&
                !Objects.equals(credentials.getConfirmPassword(), credentials.getPassword())) {
            bindingResult.rejectValue("confirmPassword",
                    "validation.confirmPassword", "Passwords do not match");
        }

        if (bindingResult.hasErrors())
            return "auth/signupPage";

        //todo request JWT from auth server

        return "redirect:/auth/signin";
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

        //todo request JWT from auth server

//        response.addCookie(jwtCookieFactory.createAuthCookie(jwt, 24 * 60 * 60));

        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        response.addCookie(jwtCookieFactory.createLogoutCookie());
        return "redirect:/";
    }

    @ModelAttribute
    public CredentialsDTO putCredentials() {
        return new CredentialsDTO();
    }

}
