package online.iwantagift.ui.web.controllers.settings;

import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.models.dto.profile.ProfileDTO;
import online.iwantagift.ui.models.payloads.ProfilePayload;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.ProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class SettingsController {
    private static final String DEFAULT_PROFILE_DESCRIPTION = "Profile description is not set yet.";

    private final CurrentUserService currentUserService;
    private final ProfileService profileService;

    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        UUID userId = currentUserService.requireUserId(authentication);
        ProfileDTO profile = profileService.getProfile(userId);

        model.addAttribute("email", currentUserService.email(authentication));
        model.addAttribute("nickname", profile.nickname() );

        model.addAttribute("profileDescription",
                profile.description() != null ?
                        profile.description() :
                        DEFAULT_PROFILE_DESCRIPTION);
        model.addAttribute("profileOwnerId", userId);
        model.addAttribute("profileAvatarUrl",
                profile.hasAvatar() ?
                        profileService.avatarUrl(userId) :
                        "/img/logo_load_error.png");

        model.addAttribute("settingsCss", List.of("/css/settings/settingsStyle.css"));

        return "settings/index";
    }

    @PostMapping("/settings/profile")
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
}
