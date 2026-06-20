package online.iwantagift.ui.util;

import online.iwantagift.ui.models.dto.wl.WishlistDTO;

import java.util.Comparator;
import java.util.List;

import static online.iwantagift.ui.services.WishlistService.DEFAULT_WISHLIST_TITLE;

public class WishlistsProcessor {

    public static void applyDisplayTitles(List<WishlistDTO> wishlists) {
        wishlists.stream()
                .filter(wl -> DEFAULT_WISHLIST_TITLE.equals(wl.getTitle()))
                .forEach(wl -> wl.setTitle("Default list"));
    }

    public static List<WishlistDTO> sortDefaultFirst(List<WishlistDTO> wishlists) {
        return wishlists.stream()
                .sorted(Comparator.comparing((WishlistDTO wishlist) ->
                                !DEFAULT_WISHLIST_TITLE.equals(wishlist.getTitle()))
                        .thenComparing(WishlistDTO::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }
}
