package online.iwantagift.ui.web.controllers;

import online.iwantagift.ui.models.dto.wl.WishDTO;
import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.models.mappers.WishMapper;
import online.iwantagift.ui.models.payloads.WishPayload;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.WishService;
import online.iwantagift.ui.services.WishlistService;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import online.iwantagift.ui.web.controllers.wl.WishController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(WishController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishService wishService;
    @MockitoBean
    private CurrentUserService currentUserService;
    @MockitoBean
    private WishlistService wishlistService;
    @MockitoBean
    private WishMapper wishMapper;

    // --- GET /wish/new ---

    @Test
    void newWishPage_returnsCreateWishView() throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/wish/new")
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/createWish"))
                .andExpect(model().attributeExists("wishPayload"))
                .andExpect(model().attributeExists("wlList"));
    }

    @Test
    void newWishPage_populatesWishlistsDropdown() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>(List.of(wishlist(listId, userId, "My List"))));

        mockMvc.perform(get("/wish/new")
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("wlList"));
    }

    // --- POST /wish/new ---

    @Test
    void createWish_validPayload_redirectsToWishlist() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(currentUserService.requireAccessToken(any())).thenReturn("token");
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/wish/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "New Gadget")
                        .param("url", "https://example.com/gadget")
                        .param("wishlistId", listId.toString())
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlist/" + listId));

        verify(wishService).createWish(any(), eq("token"));
    }

    @Test
    void createWish_noWishlistId_redirectsToProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(currentUserService.requireAccessToken(any())).thenReturn("token");
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/wish/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "New Gadget")
                        .param("url", "https://example.com/gadget")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void createWish_blankTitle_returnsCreateWishViewWithErrors(String title) throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/wish/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", title)
                        .param("url", "https://example.com")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/createWish"))
                .andExpect(model().attributeHasFieldErrors("wishPayload", "title"));

        verifyNoInteractions(wishService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-url", "javascript:alert(1)", ""})
    void createWish_invalidUrl_returnsCreateWishViewWithErrors(String url) throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/wish/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Valid Title")
                        .param("url", url)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/createWish"))
                .andExpect(model().attributeHasFieldErrors("wishPayload", "url"));

        verifyNoInteractions(wishService);
    }

    @Test
    void createWish_serviceThrows_returns500() throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(currentUserService.requireAccessToken(any())).thenReturn("token");
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>());
        when(wishService.createWish(any(), any())).thenThrow(new RemoteServiceException("down"));

        mockMvc.perform(post("/wish/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "New Gadget")
                        .param("url", "https://example.com/gadget")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("error/500"));
    }

    // --- GET /wish/{wishId}/edit ---

    @Test
    void editWishPage_owner_rendersEditView() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID wishId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        WishDTO wish = wish(wishId, userId, listId);
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishService.findWishById(wishId)).thenReturn(wish);
        when(wishMapper.toWishPayload(wish)).thenReturn(new WishPayload());
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/wish/{wishId}/edit", wishId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/editWish"))
                .andExpect(model().attributeExists("wishPayload"))
                .andExpect(model().attribute("wishId", wishId));
    }

    @Test
    void editWishPage_notOwner_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID wishId = UUID.randomUUID();
        WishDTO wish = wish(wishId, UUID.randomUUID(), UUID.randomUUID());
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishService.findWishById(wishId)).thenReturn(wish);

        mockMvc.perform(get("/wish/{wishId}/edit", wishId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }

    @Test
    void editWishPage_notFound_returns404() throws Exception {
        UUID wishId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(UUID.randomUUID());
        when(wishService.findWishById(wishId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/wish/{wishId}/edit", wishId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    @Test
    void editWishPage_serviceThrows_returns500() throws Exception {
        UUID wishId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(UUID.randomUUID());
        when(wishService.findWishById(wishId)).thenThrow(new RemoteServiceException("down"));

        mockMvc.perform(get("/wish/{wishId}/edit", wishId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("error/500"));
    }

    // --- POST /wish/{wishId}/edit ---

    @Test
    void updateWish_validPayload_redirectsToWishlist() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID wishId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        WishDTO wish = wish(wishId, userId, listId);
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(currentUserService.requireAccessToken(any())).thenReturn("token");
        when(wishService.findWishById(wishId)).thenReturn(wish);
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/wish/{wishId}/edit", wishId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated Gadget")
                        .param("url", "https://example.com/updated")
                        .param("wishlistId", listId.toString())
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlist/" + listId));

        verify(wishService).updateWish(any(), eq("token"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void updateWish_blankTitle_returnsEditViewWithErrors(String title) throws Exception {
        UUID userId = UUID.randomUUID();
        UUID wishId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/wish/{wishId}/edit", wishId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", title)
                        .param("url", "https://example.com")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/editWish"))
                .andExpect(model().attributeHasFieldErrors("wishPayload", "title"))
                .andExpect(model().attribute("wishId", wishId));
    }

    @Test
    void updateWish_notOwner_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID wishId = UUID.randomUUID();
        WishDTO wish = wish(wishId, UUID.randomUUID(), UUID.randomUUID());
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishService.findWishById(wishId)).thenReturn(wish);

        mockMvc.perform(post("/wish/{wishId}/edit", wishId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated")
                        .param("url", "https://example.com")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }

    @Test
    void updateWish_serviceThrowsOnUpdate_returnsEditView() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID wishId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        WishDTO wish = wish(wishId, userId, listId);
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(currentUserService.requireAccessToken(any())).thenReturn("token");
        when(wishService.findWishById(wishId)).thenReturn(wish);
        doThrow(new RemoteServiceException("down")).when(wishService).updateWish(any(), any());
        when(wishlistService.getAllWishlists(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/wish/{wishId}/edit", wishId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated Gadget")
                        .param("url", "https://example.com/updated")
                        .param("wishlistId", listId.toString())
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/editWish"));
    }

    private WishDTO wish(UUID id, UUID ownerId, UUID listId) {
        WishDTO dto = new WishDTO();
        dto.setId(id);
        dto.setOwnerId(ownerId);
        dto.setWishListId(listId);
        dto.setTitle("Some Wish");
        dto.setUrl("https://example.com");
        return dto;
    }

    private WishlistDTO wishlist(UUID id, UUID ownerId, String title) {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(id);
        dto.setOwnerId(ownerId);
        dto.setTitle(title);
        return dto;
    }
}
