package online.iwantagift.auth.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.services.PasswordResetService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Serves the unauthenticated password-reset pages and handles their form submissions.
 *
 * <p>The flow is: request a reset (email a confirmation link) → open the link → confirm or cancel.
 * Only on confirmation is a new password generated and emailed.
 */
@Controller
@RequestMapping("/auth/reset/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping
    public String requestForm() {
        return "reset/request";
    }

    @PostMapping
    public String request(@RequestParam String email, Model model, HttpServletResponse response) {
        PasswordResetService.RequestOutcome outcome = passwordResetService.request(email);

        if (outcome == PasswordResetService.RequestOutcome.ALREADY_PENDING) {
            model.addAttribute("error",
                    "A password reset is already in progress for this account. " +
                            "Please use the link we already emailed you, or wait for it to expire.");
            response.setStatus(HttpStatus.CONFLICT.value());
            return "reset/request";
        }

        model.addAttribute("submitted", true);
        return "reset/request";
    }

    @GetMapping("/confirm")
    public String confirmForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset/confirm";
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam String token, Model model) {
        boolean reset = passwordResetService.confirm(token);
        model.addAttribute("status", reset ? "sent" : "invalid");
        return "reset/confirm";
    }

    @PostMapping("/cancel")
    public String cancel(@RequestParam String token, Model model) {
        passwordResetService.cancel(token);
        model.addAttribute("status", "cancelled");
        return "reset/confirm";
    }
}
