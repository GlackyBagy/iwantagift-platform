package online.iwantagift.ui.web.controllers.wl;

import lombok.extern.slf4j.Slf4j;
import online.iwantagift.ui.models.dto.wl.WishDTO;
import online.iwantagift.ui.models.mappers.WishMapper;
import online.iwantagift.ui.models.payloads.WishPayload;
import online.iwantagift.ui.models.validation.WishValidationGroups;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.WishService;
import online.iwantagift.ui.services.WishlistService;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
@RequestMapping("/wish")
@Slf4j
public class WishController {

    private final WishService wishService;
    private final CurrentUserService currentUserService;
    private final WishlistService wishlistService;
    private final WishMapper wishMapper;

    public WishController(WishService wishService, CurrentUserService currentUserService, WishlistService wishlistService, WishMapper wishMapper) {
        this.wishService = wishService;
        this.currentUserService = currentUserService;
        this.wishlistService = wishlistService;
        this.wishMapper = wishMapper;
    }

    @GetMapping("/new")
    public String createWish(Model model, Authentication authentication) {
        model.addAttribute("wishPayload", new WishPayload());
        addWishlists(model, authentication);
        return "wishlists/createWish";
    }

    @PostMapping("/new")
    public String createWish(@ModelAttribute @Validated(WishValidationGroups.CreateWish.class)
                             WishPayload wishPayload, BindingResult bindingResult, Model model,
                             Authentication authentication) {
        if (bindingResult.hasErrors()) {
            addWishlists(model, authentication);
            return "wishlists/createWish";
        }

        UUID userId = currentUserService.requireUserId(authentication);
        wishPayload.setOwnerId(userId);

        UUID createdWishId;
        try {
            createdWishId = wishService.createWish(
                    wishPayload, currentUserService.requireAccessToken(authentication));
        } catch (RemoteServiceException e) {
            log.warn(e.getMessage());
            return "error/500";
        }
        UUID wishlistId = wishPayload.getWishlistId();

        if (wishlistId == null)
            return "redirect:/wish/" + createdWishId;

        return "redirect:/wishlist/" + wishlistId;
    }

    @GetMapping("/{wishId}/edit")
    public String editWish(@PathVariable UUID wishId, Model model, Authentication authentication) {
        WishLookup lookup = findOwnedWish(wishId, authentication);
        if (lookup.errorView() != null)
            return lookup.errorView();

        model.addAttribute("wishPayload", wishMapper.toWishPayload(lookup.wish()));
        model.addAttribute("wishId", wishId);
        addWishlists(model, authentication);

        return "wishlists/editWish";
    }

    @PostMapping("/{wishId}/edit")
    public String editWish(@ModelAttribute @Validated(value = WishValidationGroups.UpdateWish.class)
                           WishPayload wishPayload, BindingResult bindingResult, @PathVariable UUID wishId,
                           Model model, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("wishId", wishId);
            addWishlists(model, authentication);
            return "wishlists/editWish";
        }

        WishLookup lookup = findOwnedWish(wishId, authentication);
        if (lookup.errorView() != null)
            return lookup.errorView();

        try {
            wishPayload.setId(wishId);
            wishService.updateWish(wishPayload, currentUserService.requireAccessToken(authentication));
            return "redirect:/wishlist/" + wishPayload.getWishlistId();
        } catch (RemoteServiceException e) {
            log.debug(e.getMessage());
            model.addAttribute("wishId", wishId);
            addWishlists(model, authentication);
            return "wishlists/editWish";
        }
    }

    private WishLookup findOwnedWish(UUID wishId, Authentication authentication) {
        UUID userId = currentUserService.requireUserId(authentication);
        WishDTO wish;

        try {
            wish = wishService.findWishById(wishId);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND))
                return WishLookup.error("error/404");

            log.warn(e.getMessage());
            return WishLookup.error("error/500");
        } catch (RemoteServiceException e) {
            log.warn(e.getMessage());
            return WishLookup.error("error/500");
        }

        if (!userId.equals(wish.getOwnerId()))
            return WishLookup.error("error/403");

        return new WishLookup(wish, null);
    }

    private void addWishlists(Model model, Authentication authentication) {
        model.addAttribute("wlList", wishlistService.getAllWishlists(
                currentUserService.requireUserId(authentication)));
    }

    private record WishLookup(WishDTO wish, String errorView) {
        private static WishLookup error(String errorView) {
            return new WishLookup(null, errorView);
        }
    }
}
