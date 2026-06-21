package online.iwantagift.auth.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.auth.config.IwagProperties;
import online.iwantagift.auth.messaging.kafka.AccountProducer;
import online.iwantagift.auth.messaging.kafka.MailRequestProducer;
import online.iwantagift.auth.models.dto.VerificationViaEmail;
import online.iwantagift.auth.models.events.DataDeleteEvent;
import online.iwantagift.auth.models.events.MailRequestEvent;
import online.iwantagift.auth.repositories.AccountRepository;
import online.iwantagift.auth.services.VerificationTokenService;
import online.iwantagift.auth.util.EmailVerificationUrlBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RequestMapping("/auth")
@RequiredArgsConstructor
@Controller
@Slf4j
public class IrreversibleController {

    private final VerificationTokenService verificationTokenService;
    private final EmailVerificationUrlBuilder urlBuilder;
    private final MailRequestProducer mailRequestProducer;
    private final AccountProducer accountProducer;
    private final AccountRepository accountRepository;
    private final IwagProperties iwagProperties;

    private String getUiBaseUrl() {
        return iwagProperties.requireService("ui").requireBaseUrl().toString();
    }

    @PostMapping("/deleteAccount")
    @ResponseBody
    public void handleDeleteAccount(Authentication authentication) {
        String email = authentication.getName();
        String token = verificationTokenService.generateAndSaveForEmail(email, email);
        String url = urlBuilder.buildForAccountDeleteConfirm(token);

        mailRequestProducer.sendAccountDeleteConfirmation(new MailRequestEvent(email, url));
    }

    @GetMapping("/deleteAccount/confirm")
    public String confirmDeletionPage(@RequestParam String token, Model model) {
        verificationTokenService.findEmailChange(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("token", token);
        model.addAttribute("uiBaseUrl", getUiBaseUrl());
        return "deleteAccount/confirm";
    }

    @PostMapping("/deleteAccount/confirm")
    public String confirmDeletion(@RequestParam String token, Model model, HttpSession session) {
        VerificationViaEmail verification = verificationTokenService.findEmailChange(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String email = verification.oldEmail();
        UUID userId = accountRepository.findIdByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        accountProducer.sendOnDelete(userId);
        verificationTokenService.revoke(token);
        session.invalidate();

        model.addAttribute("token", token);
        model.addAttribute("status", "deleted");
        model.addAttribute("uiBaseUrl", getUiBaseUrl());
        return "deleteAccount/confirm";
    }

    @PostMapping("/deleteAll")
    @ResponseBody
    public void handleDeleteAll(Authentication authentication) {
        String email = authentication.getName();
        String token = verificationTokenService.generateAndSaveForEmail(email, email);
        String url = urlBuilder.buildForDataDeleteConfirm(token);

        mailRequestProducer.sendDataDeleteConfirmation(new MailRequestEvent(email, url));
    }

    @GetMapping("/deleteAll/confirm")
    public String confirmDataDeletionPage(@RequestParam String token, Model model) {
        verificationTokenService.findEmailChange(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("token", token);
        model.addAttribute("uiBaseUrl", getUiBaseUrl());
        return "deleteAll/confirm";
    }

    @PostMapping("/deleteAll/confirm")
    public String confirmDataDeletion(@RequestParam String token, Model model) {
        VerificationViaEmail verification = verificationTokenService.findEmailChange(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String email = verification.oldEmail();
        UUID userId = accountRepository.findIdByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        mailRequestProducer.sendDataDeleteRequest(new DataDeleteEvent(userId));
        verificationTokenService.revoke(token);

        model.addAttribute("token", token);
        model.addAttribute("status", "deleted");
        model.addAttribute("uiBaseUrl", getUiBaseUrl());
        return "deleteAll/confirm";
    }
}
