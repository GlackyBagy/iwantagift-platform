package online.iwantagift.ui.web.controllers.profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.ui.models.dto.profile.ProfileDTO;
import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.ProfileService;
import online.iwantagift.ui.services.WishlistService;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private static final String DEFAULT_WISHLIST_TITLE = "DEFAULT_WISHLIST";
    private static final String DEFAULT_PROFILE_DESCRIPTION = "Profile description is not set yet.";

    private final WishlistService wishlistService;
    private final ProfileService profileService;
    private final CurrentUserService currentUserService;

    @GetMapping("/profile")
    public String profile(@RequestParam(required = false) UUID listId,
                          Model model,
                          Authentication authentication) {
        UUID userId = currentUserService.requireUserId(authentication);

        return renderProfile(userId, listId, model, "profile/own", true);
    }

    @GetMapping("/profile/{profileOwnerId}")
    public String foreignProfile(@PathVariable UUID profileOwnerId,
                                 @RequestParam(required = false) UUID listId,
                                 Model model,
                                 Authentication authentication) {
        UUID currentUserId = currentUserService.requireUserId(authentication);
        if (currentUserId.equals(profileOwnerId))
            return listId == null ? "redirect:/profile" : "redirect:/profile?listId=" + listId;

        return renderProfile(profileOwnerId, listId, model, "profile/foreign", false);
    }

    private String renderProfile(UUID profileOwnerId,
                                 UUID listId,
                                 Model model,
                                 String template,
                                 boolean allowEmptyWishlists) {
        List<WishlistDTO> wishlists;

        try {
            wishlists = wishlistService.getAllWishlists(profileOwnerId);
        } catch (RemoteServiceException e) {
            log.warn(e.getMessage());
            return "error/500";
        }

        Optional<WishlistDTO> selectedWishlist = selectWishlist(wishlists, listId);
        // Own profile may have no wishlists yet — render an empty state instead of 403.
        if (selectedWishlist.isEmpty() && !(allowEmptyWishlists && wishlists.isEmpty()))
            return "error/403";

        ProfileDTO profile = profileService.getProfile(profileOwnerId);

        model.addAttribute("profileOwnerId", profileOwnerId);
        model.addAttribute("profileNickname", profile.nickname());
        model.addAttribute("profileDescription",
                profile.description() != null ?
                        profile.description() :
                        DEFAULT_PROFILE_DESCRIPTION);
        model.addAttribute("profileAvatarUrl", profile.hasAvatar() ?
                profileService.avatarUrl(profileOwnerId) :
                "/img/logo_load_error.png");
        model.addAttribute("wishlists", sortDefaultFirst(wishlists));
        model.addAttribute("selectedWishlist", selectedWishlist.orElse(null));
        model.addAttribute("profileCss", List.of("/css/profile/profileStyle.css"));

        return template;
    }

    private Optional<WishlistDTO> selectWishlist(List<WishlistDTO> wishlists, UUID requestedListId) {
        if (requestedListId != null) {
            return wishlists.stream()
                    .filter(wishlist -> requestedListId.equals(wishlist.getId()))
                    .findFirst();
        }

        return wishlists.stream()
                .filter(wishlist -> DEFAULT_WISHLIST_TITLE.equals(wishlist.getTitle()))
                .findFirst()
                .or(() -> wishlists.stream().findFirst());
    }

    private List<WishlistDTO> sortDefaultFirst(List<WishlistDTO> wishlists) {
        return wishlists.stream()
                .sorted(Comparator.comparing((WishlistDTO wishlist) ->
                                !DEFAULT_WISHLIST_TITLE.equals(wishlist.getTitle()))
                        .thenComparing(WishlistDTO::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }
}
