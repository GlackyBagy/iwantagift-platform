package online.iwantagift.ui.web.controllers.profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.WishlistService;
import online.iwantagift.ui.util.WishlistsProcessor;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static online.iwantagift.ui.services.WishlistService.DEFAULT_WISHLIST_TITLE;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {


    private final WishlistService wishlistService;
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
        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        if (isAuthenticated) {
            UUID currentUserId = currentUserService.requireUserId(authentication);
            if (currentUserId.equals(profileOwnerId))
                return listId == null ? "redirect:/profile" : "redirect:/profile?listId=" + listId;
        }

        return renderProfile(profileOwnerId, listId, model, "profile/foreign", false);
    }

    private String renderProfile(UUID profileOwnerId,
                                 UUID listId,
                                 Model model,
                                 String template,
                                 boolean allowEmptyWishlists) {
        // The profile hero (avatar/nickname/description) is hydrated client-side via
        // /api/profile/{id}, so the profile service being down only affects the hero, not this page.
        model.addAttribute("profileOwnerId", profileOwnerId);
        model.addAttribute("profileCss", List.of("/css/profile/profileStyle.css"));

        List<WishlistDTO> wishlists;
        try {
            wishlists = wishlistService.getAllWishlists(profileOwnerId);
        } catch (RemoteServiceException | ResourceAccessException | ResponseStatusException e) {
            // Wishlist service unavailable (5xx, timeout, connection refused, 503): render a
            // degraded page with an "unavailable" wishlists area instead of failing the whole page.
            log.warn("Wishlists unavailable for {}: {}", profileOwnerId, e.getMessage());
            model.addAttribute("wishlistsUnavailable", true);
            model.addAttribute("wishlists", List.of());
            model.addAttribute("selectedWishlist", null);
            return template;
        }

        Optional<WishlistDTO> selectedWishlist = selectWishlist(wishlists, listId);
        // Own profile may have no wishlists yet — render an empty state instead of 403.
        if (selectedWishlist.isEmpty() && !(allowEmptyWishlists && wishlists.isEmpty()))
            return "error/403";

        List<WishlistDTO> displayWishlists = WishlistsProcessor.sortDefaultFirst(wishlists);
        WishlistsProcessor.applyDisplayTitles(displayWishlists);

        model.addAttribute("wishlistsUnavailable", false);
        model.addAttribute("wishlists", displayWishlists);
        model.addAttribute("selectedWishlist", selectedWishlist.orElse(null));

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
}
