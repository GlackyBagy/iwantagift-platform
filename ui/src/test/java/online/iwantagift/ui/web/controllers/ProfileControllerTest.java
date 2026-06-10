package online.iwantagift.ui.web.controllers;

import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.WishlistService;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import online.iwantagift.ui.web.controllers.profile.ProfileController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishlistService wishlistService;
    @MockitoBean
    private CurrentUserService currentUserService;

    @Test
    void ownProfile_rendersOwnTemplate() throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishlistService.getAllWishlists(userId)).thenReturn(List.of(wishlist(userId, "My list")));

        mockMvc.perform(get("/profile")
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/own"))
                .andExpect(model().attribute("wishlistsUnavailable", false))
                .andExpect(model().attribute("profileOwnerId", userId))
                .andExpect(model().attributeExists("selectedWishlist"))
                // Profile hero is hydrated client-side, so no server-side profile attributes.
                .andExpect(model().attributeDoesNotExist("profileNickname", "profileAvatarUrl"));
    }

    @Test
    void ownProfile_wishlistServiceDown_rendersDegradedNot500() throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishlistService.getAllWishlists(userId)).thenThrow(new RemoteServiceException("down"));

        mockMvc.perform(get("/profile")
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/own"))
                .andExpect(model().attribute("wishlistsUnavailable", true));
    }

    @Test
    void foreignProfile_wishlistServiceDown_rendersDegradedNot500() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(currentUserId);
        when(wishlistService.getAllWishlists(ownerId)).thenThrow(new RemoteServiceException("down"));

        mockMvc.perform(get("/profile/{ownerId}", ownerId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/foreign"))
                .andExpect(model().attribute("wishlistsUnavailable", true));
    }

    @Test
    void foreignProfile_ofSelf_redirectsToOwnProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);

        mockMvc.perform(get("/profile/{ownerId}", userId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    private WishlistDTO wishlist(UUID ownerId, String title) {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(UUID.randomUUID());
        dto.setTitle(title);
        dto.setOwnerId(ownerId);
        return dto;
    }
}
