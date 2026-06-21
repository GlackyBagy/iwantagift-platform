package online.iwantagift.ui.utils;

import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.util.WishlistsProcessor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class WishlistsProcessorTest {

    private static final String DEFAULT_TITLE = "DEFAULT_WISHLIST";

    // --- applyDisplayTitles ---

    @Test
    void applyDisplayTitles_renamesDefaultWishlist() {
        WishlistDTO wl = wishlist("DEFAULT_WISHLIST");
        WishlistsProcessor.applyDisplayTitles(List.of(wl));
        assertThat(wl.getTitle()).isEqualTo("Default list");
    }

    @Test
    void applyDisplayTitles_doesNotRenameOtherWishlists() {
        WishlistDTO wl = wishlist("My Gifts");
        WishlistsProcessor.applyDisplayTitles(List.of(wl));
        assertThat(wl.getTitle()).isEqualTo("My Gifts");
    }

    @Test
    void applyDisplayTitles_handlesEmptyList() {
        WishlistsProcessor.applyDisplayTitles(List.of());
    }

    @Test
    void applyDisplayTitles_onlyRenamesDefaultEntries() {
        WishlistDTO def = wishlist("DEFAULT_WISHLIST");
        WishlistDTO custom = wishlist("Custom");
        WishlistsProcessor.applyDisplayTitles(List.of(def, custom));
        assertThat(def.getTitle()).isEqualTo("Default list");
        assertThat(custom.getTitle()).isEqualTo("Custom");
    }

    // --- sortDefaultFirst ---

    @Test
    void sortDefaultFirst_putsDefaultWishlistFirst() {
        WishlistDTO alpha = wishlist("Alpha");
        WishlistDTO def = wishlist("DEFAULT_WISHLIST");
        WishlistDTO zeta = wishlist("Zeta");

        List<WishlistDTO> sorted = WishlistsProcessor.sortDefaultFirst(new ArrayList<>(List.of(alpha, zeta, def)));

        assertThat(sorted.get(0).getTitle()).isEqualTo("DEFAULT_WISHLIST");
    }

    @Test
    void sortDefaultFirst_nonDefaultEntriesSortedAlphabetically() {
        WishlistDTO zeta = wishlist("Zeta");
        WishlistDTO alpha = wishlist("Alpha");
        WishlistDTO mango = wishlist("Mango");

        List<WishlistDTO> sorted = WishlistsProcessor.sortDefaultFirst(new ArrayList<>(List.of(zeta, mango, alpha)));

        assertThat(sorted).extracting(WishlistDTO::getTitle)
                .containsExactly("Alpha", "Mango", "Zeta");
    }

    @Test
    void sortDefaultFirst_defaultAndOthersMixed_defaultAlwaysFirst() {
        WishlistDTO a = wishlist("Apple");
        WishlistDTO def = wishlist("DEFAULT_WISHLIST");
        WishlistDTO b = wishlist("Banana");

        List<WishlistDTO> sorted = WishlistsProcessor.sortDefaultFirst(new ArrayList<>(List.of(a, def, b)));

        assertThat(sorted.get(0).getTitle()).isEqualTo("DEFAULT_WISHLIST");
        assertThat(sorted).extracting(WishlistDTO::getTitle)
                .containsExactly("DEFAULT_WISHLIST", "Apple", "Banana");
    }

    @Test
    void sortDefaultFirst_emptyList_returnsEmptyList() {
        List<WishlistDTO> sorted = WishlistsProcessor.sortDefaultFirst(new ArrayList<>());
        assertThat(sorted).isEmpty();
    }

    @Test
    void sortDefaultFirst_singleDefault_returnsIt() {
        WishlistDTO def = wishlist("DEFAULT_WISHLIST");
        List<WishlistDTO> sorted = WishlistsProcessor.sortDefaultFirst(new ArrayList<>(List.of(def)));
        assertThat(sorted).hasSize(1);
        assertThat(sorted.get(0).getTitle()).isEqualTo("DEFAULT_WISHLIST");
    }

    @Test
    void sortDefaultFirst_doesNotMutateInput() {
        WishlistDTO zeta = wishlist("Zeta");
        WishlistDTO alpha = wishlist("Alpha");
        List<WishlistDTO> input = new ArrayList<>(List.of(zeta, alpha));

        WishlistsProcessor.sortDefaultFirst(input);

        assertThat(input).extracting(WishlistDTO::getTitle).containsExactly("Zeta", "Alpha");
    }

    private WishlistDTO wishlist(String title) {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(UUID.randomUUID());
        dto.setTitle(title);
        return dto;
    }
}
