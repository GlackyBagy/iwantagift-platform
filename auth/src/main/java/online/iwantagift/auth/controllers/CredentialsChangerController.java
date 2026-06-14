package online.iwantagift.auth.controllers;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.messaging.kafka.CredentialsProducer;
import online.iwantagift.auth.models.dto.CredentialsDTO;
import online.iwantagift.auth.models.dto.abstracts.ValidationGroups;
import online.iwantagift.auth.models.events.CredentialsUpdateEvent;
import online.iwantagift.auth.services.AccountService;
import online.iwantagift.auth.services.VerificationTokenService;
import online.iwantagift.auth.util.EmailVerificationUrlBuilder;
import online.iwantagift.auth.util.exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import static online.iwantagift.auth.models.dto.abstracts.ValidationGroups.ChangeEmail;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CredentialsChangerController {

    private final VerificationTokenService verificationTokenService;
    private final EmailVerificationUrlBuilder emailVerificationUrlBuilder;
    private final CredentialsProducer credentialsProducer;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    @PostMapping("/change/email")
    public void changeEmail(@RequestBody @Validated(ChangeEmail.class)
                            CredentialsDTO dto,
                            Authentication authentication) {
        String oldEmail = authentication.getName();
        String token = verificationTokenService.generateAndSaveForEmail(oldEmail, dto.getEmail());
        String verificationUrl = emailVerificationUrlBuilder.buildForEmail(token);

        credentialsProducer.sendEmailChange(new CredentialsUpdateEvent(
                oldEmail,
                dto.getEmail(),
                verificationUrl
        ));
    }

    @PostMapping("/confirm/email")
    public void confirmEmail(Authentication authentication) {
        String email = authentication.getName();
        String token = verificationTokenService.generateAndSaveForEmail(email, email);
        String verificationUrl = emailVerificationUrlBuilder.buildForEmail(token);

        credentialsProducer.sendEmailVerify(new CredentialsUpdateEvent(
                email,
                email,
                verificationUrl
        ));
    }

    /**
     * Changes the password of the authenticated account directly.
     *
     * <p>No email confirmation is involved: the only check is that the request is authenticated and
     * the supplied password is valid and confirmed.
     */
    @PostMapping("/change/password")
    public void changePassword(@RequestBody @Validated(ValidationGroups.ChangePassword.class)
                               CredentialsDTO dto, BindingResult bindingResult,
                               Authentication authentication) {
        if (Objects.nonNull(dto.getConfirmPassword()) &&
                !Objects.equals(dto.getConfirmPassword(), dto.getPassword())) {
            bindingResult.rejectValue("confirmPassword",
                    "validation.confirmPassword", "Passwords do not match");
        }
        if (bindingResult.hasErrors())
            throw new ValidationException(bindingResult);

        String email = authentication.getName();
        String passwordHash = passwordEncoder.encode(dto.getPassword());

        if (!accountService.updatePassword(email, passwordHash))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private void handle(MethodArgumentNotValidException e) {
        throw new ValidationException(e);
    }
}
