package online.iwantagift.auth.controllers;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.messaging.kafka.AccountProducer;
import online.iwantagift.auth.models.events.AccountEvent;
import online.iwantagift.auth.models.dto.CredentialsDTO;
import online.iwantagift.auth.models.dto.abstracts.ValidationGroups;
import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.models.entities.AccountFactory;
import online.iwantagift.auth.services.AccountService;
import online.iwantagift.auth.util.exceptions.EmailAlreadyExistsException;
import online.iwantagift.auth.util.exceptions.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Exposes account registration endpoints.
 *
 * <p>Token issuance is handled by Spring Authorization Server OAuth2 endpoints.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AccountService accountService;
    private final AccountFactory accountFactory;
    private final AccountProducer accountProducer;

    /**
     * Registers a new account.
     *
     * @param credentials   registration payload containing user credentials
     * @param bindingResult validation result for the incoming payload
     * @return public account data for the created user
     * @throws ValidationException if validation fails or passwords do not match
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountEvent signUp(@RequestBody @Validated(ValidationGroups.SignUp.class)
                                  CredentialsDTO credentials, BindingResult bindingResult) {
        if (Objects.nonNull(credentials.getConfirmPassword()) &&
                !Objects.equals(credentials.getConfirmPassword(), credentials.getPassword())) {
            bindingResult.rejectValue("confirmPassword",
                    "validation.confirmPassword", "Passwords do not match");
        }

        if (bindingResult.hasErrors())
            throw new ValidationException(bindingResult);

        Account account = accountFactory.create(credentials);
        try {
            accountService.save(account);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException();
        }
        accountProducer.sendOnCreate(account.getId(), credentials.getNickname(), account.getEmail()); // todo guaranty DB + kafka operations atomicity

        return new AccountEvent(account.getId(), credentials.getNickname(), account.getEmail(), AccountEvent.Type.CREATED);
    }
}
