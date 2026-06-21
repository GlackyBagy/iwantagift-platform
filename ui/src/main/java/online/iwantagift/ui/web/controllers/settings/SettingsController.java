package online.iwantagift.ui.web.controllers.settings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.ui.models.dto.auth.CredentialsDTO;
import online.iwantagift.ui.models.payloads.ProfilePayload;
import online.iwantagift.ui.models.validation.AuthValidationGroups;
import online.iwantagift.ui.services.AuthService;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.ProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
@Slf4j
public class SettingsController {

    private final CurrentUserService currentUserService;
    private final ProfileService profileService;
    private final AuthService authService;

    @GetMapping
    public String settings(Model model, Authentication authentication) {
        UUID userId = currentUserService.requireUserId(authentication);

        model.addAttribute("email", currentUserService.email(authentication));
        model.addAttribute("profileOwnerId", userId);
        model.addAttribute("settingsCss", List.of("/css/settings/settingsStyle.css"));

        return "settings/index";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute @Validated ProfilePayload profilePayload,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("profileValidationFailed", true);
            return "redirect:/settings";
        }

        String accessToken = currentUserService.requireAccessToken(authentication);
        profileService.updateProfile(profilePayload, accessToken);

        redirectAttributes.addFlashAttribute("profileUpdated", true);
        redirectAttributes.addFlashAttribute("profilePhotoReceived",
                profilePayload.getProfilePhoto() != null && !profilePayload.getProfilePhoto().isEmpty());

        return "redirect:/settings";
    }

    @PostMapping("/changeEmail")
    public String changeEmail(@ModelAttribute @Validated(AuthValidationGroups.ChangeEmail.class)
                              CredentialsDTO credentialsDTO, BindingResult bindingResult,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFieldErrors(bindingResult, "email", "emailValidationErrors", redirectAttributes);
            return "redirect:/settings";
        }

        String accessToken = currentUserService.requireAccessToken(authentication);

        authService.sendEmailChangeMessage(accessToken, credentialsDTO);

        log.info("Sent email change request");
        return "redirect:/settings"; // todo js alert
    }

    @PostMapping("/changePassword")
    public String changePassword(@ModelAttribute @Validated(AuthValidationGroups.ChangePassword.class)
                                 CredentialsDTO credentialsDTO, BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (credentialsDTO.getPassword() != null &&
                !credentialsDTO.getPassword().equals(credentialsDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword",
                    "validation.confirmPassword", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            addFieldErrors(bindingResult, "password", "passwordValidationErrors", redirectAttributes);
            addFieldErrors(bindingResult, "confirmPassword",
                    "confirmPasswordValidationErrors", redirectAttributes);
            return "redirect:/settings";
        }

        String accessToken = currentUserService.requireAccessToken(authentication);

        authService.sendPasswordChangeMessage(accessToken, credentialsDTO);

        return "redirect:/settings"; // todo js alert
    }

    @PostMapping("/confirmEmail")
    public String confirmEmail(@ModelAttribute @Validated(AuthValidationGroups.ConfirmEmail.class)
                               CredentialsDTO credentialsDTO, BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFieldErrors(bindingResult, "email", "emailValidationErrors", redirectAttributes);
            return "redirect:/settings";
        }
        String accessToken = currentUserService.requireAccessToken(authentication);

        authService.sendEmailConfirmationMessage(accessToken);

        return "redirect:/settings"; // todo js alert
    }

    private void addFieldErrors(BindingResult bindingResult, String field,
                                String attribute, RedirectAttributes redirectAttributes) {
        List<String> messages = bindingResult.getFieldErrors(field).stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        if (!messages.isEmpty()) {
            redirectAttributes.addFlashAttribute(attribute, messages);
        }
    }
}
