package online.iwantagift.auth.controllers;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.models.dto.CredentialsDTO;
import online.iwantagift.auth.models.dto.abstracts.ValidationGroups;
import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.models.entities.AccountFactory;
import online.iwantagift.auth.services.AccountService;
import online.iwantagift.auth.services.JwtService;
import online.iwantagift.auth.util.exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountService accountService;
    private final AccountFactory accountFactory;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    public String signUp(@RequestBody @Validated(ValidationGroups.SignIn.class)
                         CredentialsDTO credentials, BindingResult bindingResult) {
        if (Objects.nonNull(credentials.getConfirmPassword()) &&
                !Objects.equals(credentials.getConfirmPassword(), credentials.getPassword())) {
            bindingResult.rejectValue("confirmPassword",
                    "validation.confirmPassword", "Passwords do not match");
        }

        if (bindingResult.hasErrors())
            throw new ValidationException(bindingResult);

        Account account = accountFactory.create(credentials);
        accountService.save(account);

        return jwtFromCredentials(credentials);
    }

    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    public String signIn(@RequestBody @Validated(ValidationGroups.SignIn.class)
                         CredentialsDTO credentials, BindingResult result) {
        if (result.hasErrors())
            throw new ValidationException(result);

        return jwtFromCredentials(credentials);
    }

    private String jwtFromCredentials(CredentialsDTO credentials) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        credentials.getEmail(),
                        credentials.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return jwtService.generateToken(Objects.requireNonNull(userDetails));
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private Map<String, String> handleValidationException(ValidationException exception) {
        Map<String, String> res = new HashMap<>();

        for (ObjectError error : exception.getResult().getAllErrors()) {
            if (error instanceof FieldError fieldError)
                res.put(fieldError.getField(), error.getDefaultMessage());
            else
                res.put(error.getObjectName(), error.getDefaultMessage());
        }

        return res;
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    private void handleBadCredentials() {
    }
}
