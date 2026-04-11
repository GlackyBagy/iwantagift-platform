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

/**
 * Exposes authentication endpoints for account registration and login.
 *
 * <p>The controller validates incoming credentials, creates accounts during sign-up, authenticates
 * users through Spring Security, and returns a JWT access token for successful requests.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountService accountService;
    private final AccountFactory accountFactory;

    /**
     * Registers a new account and returns a JWT for the created user.
     *
     * @param credentials registration payload containing user credentials
     * @param bindingResult validation result for the incoming payload
     * @return signed JWT access token for the newly registered user
     * @throws ValidationException if validation fails or passwords do not match
     */
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

    /**
     * Authenticates an existing user and returns a JWT for the authenticated account.
     *
     * @param credentials sign-in payload containing email and password
     * @param result validation result for the incoming payload
     * @return signed JWT access token for the authenticated user
     * @throws ValidationException if request validation fails
     */
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

    /**
     * Converts validation errors into a field-to-message response body.
     *
     * @param exception validation exception containing binding errors
     * @return map of field or object names to validation error messages
     */
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
    private void handleBadCredentials() { //todo
    }
}
