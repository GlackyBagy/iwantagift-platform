package online.iwantagift.ui.web.controllers.wl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.ui.models.dto.wl.WishDTO;
import online.iwantagift.ui.models.mappers.WishMapper;
import online.iwantagift.ui.models.payloads.WishPayload;
import online.iwantagift.ui.models.validation.WishValidationGroups;
import online.iwantagift.ui.services.JwtService;
import online.iwantagift.ui.services.WishService;
import online.iwantagift.ui.services.WishlistService;
import online.iwantagift.ui.util.exceptions.NotFoundException;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import online.iwantagift.ui.util.exceptions.ServiceUnauthorizedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/wish")
@Slf4j
public class WishController {

    private final WishService wishService;
    private final JwtService jwtService;
    private final WishlistService wishlistService;
    private final WishMapper wishMapper;

    public WishController(WishService wishService, JwtService jwtService, WishlistService wishlistService, WishMapper wishMapper) {
        this.wishService = wishService;
        this.jwtService = jwtService;
        this.wishlistService = wishlistService;
        this.wishMapper = wishMapper;
    }

    @GetMapping("/new")
    public String createWish(Model model, HttpServletRequest request) {
        model.addAttribute("wishPayload", new WishPayload());
        addWishlists(model, request);
        return "wishlists/createWish";
    }

    @PostMapping("/new")
    public String createWish(@ModelAttribute @Validated(WishValidationGroups.CreateWish.class)
                             WishPayload wishPayload, BindingResult bindingResult, Model model,
                             HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            addWishlists(model, request);
            return "wishlists/createWish";
        }

        UUID userId = jwtService.retrieveUserIdFromCookie(request).get();
        wishPayload.setOwnerId(userId);

        UUID createdWishId;
        try {
            createdWishId = wishService.createWish(wishPayload);
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
    public String editWish(@PathVariable UUID wishId, Model model, HttpServletRequest request) {
        WishLookup lookup = findOwnedWish(wishId, request);
        if (lookup.errorView() != null)
            return lookup.errorView();

        model.addAttribute("wishPayload", wishMapper.toWishPayload(lookup.wish()));
        model.addAttribute("wishId", wishId);
        addWishlists(model, request);

        return "wishlists/editWish";
    }

    @PostMapping("/{wishId}/edit")
    public String editWish(@ModelAttribute @Validated(value = WishValidationGroups.UpdateWish.class)
                           WishPayload wishPayload, BindingResult bindingResult, @PathVariable UUID wishId,
                           Model model, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("wishId", wishId);
            addWishlists(model, request);
            return "wishlists/editWish";
        }

        WishLookup lookup = findOwnedWish(wishId, request);
        if (lookup.errorView() != null)
            return lookup.errorView();

        try {
            wishPayload.setId(wishId);
            wishService.updateWish(wishPayload);
            return "redirect:/wishlist/" + wishPayload.getWishlistId();
        } catch (ServiceUnauthorizedException e) {
            log.warn(e.getMessage());
            return "error/500";
        } catch (NotFoundException e) {
            log.debug(e.getMessage());
            return "error/404";
        } catch (RemoteServiceException e) {
            log.debug(e.getMessage());
            model.addAttribute("wishId", wishId);
            addWishlists(model, request);
            return "wishlists/editWish";
        }
    }

    private WishLookup findOwnedWish(UUID wishId, HttpServletRequest request) {
        UUID userId = jwtService.retrieveUserIdFromCookie(request).get();
        WishDTO wish;

        try {
            wish = wishService.findWishById(wishId);
        } catch (NotFoundException e) {
            return WishLookup.error("error/404");
        } catch (RemoteServiceException e) {
            log.warn(e.getMessage());
            return WishLookup.error("error/500");
        }

        if (!userId.equals(wish.getOwnerId()))
            return WishLookup.error("error/403");

        return new WishLookup(wish, null);
    }

    private void addWishlists(Model model, HttpServletRequest request) {
        model.addAttribute("wlList", wishlistService.getAllWishlists(
                jwtService.retrieveUserIdFromCookie(request).get()));
    }

    private record WishLookup(WishDTO wish, String errorView) {
        private static WishLookup error(String errorView) {
            return new WishLookup(null, errorView);
        }
    }
}
