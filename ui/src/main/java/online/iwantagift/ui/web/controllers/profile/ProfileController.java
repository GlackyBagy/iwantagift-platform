package online.iwantagift.ui.web.controllers.profile;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.services.JwtService;
import online.iwantagift.ui.services.WishlistService;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
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
    private final JwtService jwtService;

    @GetMapping("/profile")
    public String profile(@RequestParam(required = false) UUID listId,
                          Model model,
                          HttpServletRequest request) {
        UUID userId = jwtService.retrieveUserIdFromCookie(request).orElseThrow(); // todo handle
        String nickname = jwtService.retrieveDisplayNameFromCookie(request).orElse("User");

        return renderProfile(userId, listId, nickname, model, "profile/own");
    }

    @GetMapping("/profile/{profileOwnerId}")
    public String foreignProfile(@PathVariable UUID profileOwnerId,
                                 @RequestParam(required = false) UUID listId,
                                 Model model,
                                 HttpServletRequest request) {
        UUID currentUserId = jwtService.retrieveUserIdFromCookie(request).orElseThrow(); // todo handle
        if (currentUserId.equals(profileOwnerId))
            return listId == null ? "redirect:/profile" : "redirect:/profile?listId=" + listId;

        return renderProfile(profileOwnerId, listId, "User", model, "profile/foreign"); //todo insert real profileNickname
    }

    private String renderProfile(UUID profileOwnerId,
                                 UUID listId,
                                 String profileNickname,
                                 Model model,
                                 String template) {
        List<WishlistDTO> wishlists;

        try {
            wishlists = wishlistService.getAllWishlists(profileOwnerId);
        } catch (RemoteServiceException e) {
            log.warn(e.getMessage());
            return "error/500";
        }

        Optional<WishlistDTO> selectedWishlist = selectWishlist(wishlists, listId);
        if (selectedWishlist.isEmpty())
            return "error/403";

        model.addAttribute("profileOwnerId", profileOwnerId);
        model.addAttribute("profileNickname", profileNickname);
        model.addAttribute("profileDescription", DEFAULT_PROFILE_DESCRIPTION);
        model.addAttribute("wishlists", sortDefaultFirst(wishlists));
        model.addAttribute("selectedWishlist", selectedWishlist.get());
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
