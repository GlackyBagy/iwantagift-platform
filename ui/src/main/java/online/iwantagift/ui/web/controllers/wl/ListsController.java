package online.iwantagift.ui.web.controllers.wl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.models.mappers.WishlistMapper;
import online.iwantagift.ui.models.payloads.WishlistPayload;
import online.iwantagift.ui.models.validation.WishlistValidationGroups;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.WishlistService;
import online.iwantagift.ui.util.exceptions.NotFoundException;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import online.iwantagift.ui.util.exceptions.ServiceUnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Slf4j
public class ListsController {
    private final WishlistService wishlistService;
    private final CurrentUserService currentUserService;
    private final WishlistMapper wishlistMapper;

    @GetMapping("/new")
    public String createWish(Model model) {
        model.addAttribute("wishlistPayload", new WishlistPayload());
        return "wishlists/createList";
    }

    @PostMapping("/new")
    public String createWish(@ModelAttribute @Validated(WishlistValidationGroups.CreateWishlist.class)
                             WishlistPayload wishlistPayload,
                             BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors())
            return "wishlists/createList";

        UUID userId = currentUserService.requireUserId(authentication);
        wishlistPayload.setOwnerId(userId);

        UUID createWishlistId;

        try {
            createWishlistId = wishlistService.createWishlist(wishlistPayload);
        } catch (RemoteServiceException e) {
            log.warn(e.getMessage());
            return "error/500";
        }

        return "redirect:/wishlist/" + createWishlistId;
    }

    @GetMapping("/{listId}")
    public String showWishlist(@PathVariable UUID listId, Model model) {
        WishlistDTO wishlist;

        try {
            wishlist = wishlistService.getWishlist(listId);
        } catch (RemoteServiceException e) {
            log.warn(e.getMessage());
            return "error/500";
        }

        model.addAttribute("wishlist", wishlist);
        return "wishlists/index";
    }

    @GetMapping("/{listId}/edit")
    public String editWishlist(@PathVariable UUID listId, Model model, Authentication authentication) {
        UUID userId = currentUserService.requireUserId(authentication);
        WishlistLookup lookup = findOwnedWishlist(listId, userId);
        if (lookup.errorView() != null)
            return lookup.errorView();

        model.addAttribute("wishlistPayload", wishlistMapper.toWishlistPayload(lookup.wishlist()));
        model.addAttribute("listId", listId);
        return "wishlists/editList";
    }

    @PostMapping("/{listId}/edit")
    public String editWishlist(@ModelAttribute @Validated(WishlistValidationGroups.UpdateWishlist.class)
                               WishlistPayload wishlistPayload,
                               BindingResult bindingResult, @PathVariable UUID listId,
                               Model model, Authentication authentication) {
        UUID userId = currentUserService.requireUserId(authentication);

        if (bindingResult.hasErrors()) {
            model.addAttribute("listId", listId);
            return "wishlists/editList";
        }

        WishlistLookup lookup = findOwnedWishlist(listId, userId);
        if (lookup.errorView() != null)
            return lookup.errorView();

        try {
            wishlistPayload.setId(listId);
            wishlistPayload.setOwnerId(userId);
            wishlistService.updateWishlist(wishlistPayload);
            return "redirect:/wishlist/" + listId;
        } catch (ServiceUnauthorizedException e) {
            log.warn(e.getMessage());
            return "error/500";
        } catch (NotFoundException e) {
            return "error/404";
        } catch (RemoteServiceException e) {
            log.warn(e.getMessage());
            model.addAttribute("listId", listId);
            return "wishlists/editList";
        }
    }

    private WishlistLookup findOwnedWishlist(UUID listId, UUID userId) {
        WishlistDTO wishlist;

        try {
            wishlist = wishlistService.getWishlist(listId);
        } catch (NotFoundException e) {
            return WishlistLookup.error("error/404");
        } catch (RemoteServiceException e) {
            log.warn(e.getMessage());
            return WishlistLookup.error("error/500");
        }

        if (!userId.equals(wishlist.getOwnerId()))
            return WishlistLookup.error("error/403");

        return new WishlistLookup(wishlist, null);
    }

    private record WishlistLookup(WishlistDTO wishlist, String errorView) {
        private static WishlistLookup error(String errorView) {
            return new WishlistLookup(null, errorView);
        }
    }
}
