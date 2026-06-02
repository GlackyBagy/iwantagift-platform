package online.iwantagift.ui.web.controllers.settings;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.IwagProperties;
import online.iwantagift.ui.models.payloads.ProfilePayload;
import online.iwantagift.ui.services.JwtService;
import online.iwantagift.ui.services.ProfileService;
import org.springframework.beans.factory.annotation.Value;
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

    private final JwtService jwtService;
    private final ProfileService profileService;
    private final IwagProperties iwagProperties;

    @Value("${jwt.cookie.name.access}")
    private String accessCookieName;

    @GetMapping("/settings")
    public String settings(Model model, HttpServletRequest request) {
        UUID userId = jwtService.retrieveUserIdFromCookie(request).orElseThrow(); // todo handle

        model.addAttribute("email", jwtService.retrieveEmailFromCookie(request).orElse(""));
        model.addAttribute("nickname", "User");
        model.addAttribute("profileDescription", DEFAULT_PROFILE_DESCRIPTION);
        model.addAttribute("profileOwnerId", userId);
        model.addAttribute("profileApiBaseUrl", iwagProperties.getRequiredService("profile").getBaseUrl());
        model.addAttribute("accessCookieName", accessCookieName);
        model.addAttribute("settingsCss", List.of("/css/settings/settingsStyle.css"));

        return "settings/index";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@ModelAttribute @Validated ProfilePayload profilePayload,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("profileValidationFailed", true);
            return "redirect:/settings";
        }

        String accessToken = jwtService.extractAccessToken(request)
                .orElseThrow(() -> new IllegalStateException("Access token cookie is missing"));
        profileService.updateProfile(profilePayload, accessToken);

        redirectAttributes.addFlashAttribute("profileUpdated", true);
        redirectAttributes.addFlashAttribute("profilePhotoReceived",
                profilePayload.getProfilePhoto() != null && !profilePayload.getProfilePhoto().isEmpty());

        return "redirect:/settings";
    }
}
