package online.iwantagift.auth.controllers;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.config.IwagProperties;
import online.iwantagift.auth.models.dto.EmailChangeVerification;
import online.iwantagift.auth.services.AccountService;
import online.iwantagift.auth.services.VerificationTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

@Controller
@RequestMapping("/auth/verify")
@RequiredArgsConstructor
public class TokenVerifierController {

    private final VerificationTokenService verificationTokenService;
    private final AccountService accountService;
    private final IwagProperties iwagProperties;
    private String settingsUrl;

    @PostConstruct
    protected void init(){
        var uiProps = iwagProperties.requireService("ui");

        this.settingsUrl = UriComponentsBuilder
                .fromUriString(uiProps.requireBaseUrl().toString())
                .path("/settings")
                .build()
                .toUriString();
    }

    @GetMapping("/email")
    public String verifyEmail(@RequestParam String token) {
        EmailChangeVerification verification = verificationTokenService.findEmailChange(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean successfullyVerified;

        if (Objects.equals(verification.oldEmail(), verification.newEmail()))
            successfullyVerified = accountService.verifyEmail(verification.oldEmail());
        else
            successfullyVerified = accountService.updateEmail(verification.oldEmail(), verification.newEmail());

        if (!successfullyVerified)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        verificationTokenService.revoke(token);

        return "redirect:" + settingsUrl;
    }
}
