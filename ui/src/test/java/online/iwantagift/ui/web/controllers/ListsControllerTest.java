package online.iwantagift.ui.web.controllers;

import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.models.mappers.WishlistMapper;
import online.iwantagift.ui.models.payloads.WishlistPayload;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.WishlistService;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import online.iwantagift.ui.web.controllers.wl.ListsController;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ListsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishlistService wishlistService;
    @MockitoBean
    private CurrentUserService currentUserService;
    @MockitoBean
    private WishlistMapper wishlistMapper;

    // --- GET /wishlist/new ---

    @Test
    void newWishlistPage_returnsCreateListView() throws Exception {
        mockMvc.perform(get("/wishlist/new").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/createList"))
                .andExpect(model().attributeExists("wishlistPayload"));
    }

    // --- POST /wishlist/new ---

    @Test
    void createWishlist_validPayload_redirectsToCreatedList() throws Exception {
        UUID listId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(UUID.randomUUID());
        when(currentUserService.requireAccessToken(any())).thenReturn("token");
        when(wishlistService.createWishlist(any(WishlistPayload.class), eq("token"))).thenReturn(listId);

        mockMvc.perform(post("/wishlist/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "My Wishlist")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlist/" + listId));

        verify(wishlistService).createWishlist(any(), eq("token"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void createWishlist_blankTitle_returnsCreateListViewWithErrors(String title) throws Exception {
        Authentication authentication = mock(Authentication.class);

        mockMvc.perform(post("/wishlist/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", title)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/createList"))
                .andExpect(model().attributeHasFieldErrors("wishlistPayload", "title"));

        verifyNoInteractions(wishlistService);
    }

    @Test
    void createWishlist_serviceThrows_returns500() throws Exception {
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(UUID.randomUUID());
        when(currentUserService.requireAccessToken(any())).thenReturn("token");
        when(wishlistService.createWishlist(any(), any())).thenThrow(new RemoteServiceException("down"));

        mockMvc.perform(post("/wishlist/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "My Wishlist")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("error/500"));
    }

    // --- GET /wishlist/{listId} ---

    @Test
    void showWishlist_rendersIndexView() throws Exception {
        UUID listId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        WishlistDTO wishlist = wishlist(listId, ownerId, "My List");

        when(wishlistService.getWishlist(listId)).thenReturn(wishlist);

        mockMvc.perform(get("/wishlist/{listId}", listId).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/index"))
                .andExpect(model().attributeExists("wishlist"));
    }

    @Test
    void showWishlist_defaultTitleDisplayedCorrectly() throws Exception {
        UUID listId = UUID.randomUUID();
        WishlistDTO wishlist = wishlist(listId, UUID.randomUUID(), "DEFAULT_WISHLIST");

        when(wishlistService.getWishlist(listId)).thenReturn(wishlist);

        mockMvc.perform(get("/wishlist/{listId}", listId).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/index"));
    }

    @Test
    void showWishlist_serviceThrows_returns500() throws Exception {
        UUID listId = UUID.randomUUID();

        when(wishlistService.getWishlist(listId)).thenThrow(new RemoteServiceException("down"));

        mockMvc.perform(get("/wishlist/{listId}", listId).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("error/500"));
    }

    // --- GET /wishlist/{listId}/edit ---

    @Test
    void editWishlistPage_owner_rendersEditView() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        WishlistDTO wishlist = wishlist(listId, userId, "My List");
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishlistService.getWishlist(listId)).thenReturn(wishlist);
        when(wishlistMapper.toWishlistPayload(wishlist)).thenReturn(new WishlistPayload());

        mockMvc.perform(get("/wishlist/{listId}/edit", listId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/editList"))
                .andExpect(model().attributeExists("wishlistPayload"))
                .andExpect(model().attribute("listId", listId));
    }

    @Test
    void editWishlistPage_notOwner_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        WishlistDTO wishlist = wishlist(listId, UUID.randomUUID(), "Other's List");
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishlistService.getWishlist(listId)).thenReturn(wishlist);

        mockMvc.perform(get("/wishlist/{listId}/edit", listId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }

    @Test
    void editWishlistPage_notFound_returns404() throws Exception {
        UUID listId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(UUID.randomUUID());
        when(wishlistService.getWishlist(listId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/wishlist/{listId}/edit", listId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    @Test
    void editWishlistPage_serviceThrows_returns500() throws Exception {
        UUID listId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(UUID.randomUUID());
        when(wishlistService.getWishlist(listId)).thenThrow(new RemoteServiceException("down"));

        mockMvc.perform(get("/wishlist/{listId}/edit", listId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("error/500"));
    }

    // --- POST /wishlist/{listId}/edit ---

    @Test
    void updateWishlist_validPayload_redirectsToList() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        WishlistDTO wishlist = wishlist(listId, userId, "My List");
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(currentUserService.requireAccessToken(any())).thenReturn("token");
        when(wishlistService.getWishlist(listId)).thenReturn(wishlist);

        mockMvc.perform(post("/wishlist/{listId}/edit", listId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", listId.toString())
                        .param("title", "Updated Title")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlist/" + listId));

        verify(wishlistService).updateWishlist(any(), eq("token"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void updateWishlist_blankTitle_returnsEditView(String title) throws Exception {
        UUID listId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(UUID.randomUUID());

        mockMvc.perform(post("/wishlist/{listId}/edit", listId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", title)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/editList"))
                .andExpect(model().attributeHasFieldErrors("wishlistPayload", "title"))
                .andExpect(model().attribute("listId", listId));
    }

    @Test
    void updateWishlist_notOwner_returns403() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        WishlistDTO wishlist = wishlist(listId, UUID.randomUUID(), "Other's List");
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(wishlistService.getWishlist(listId)).thenReturn(wishlist);

        mockMvc.perform(post("/wishlist/{listId}/edit", listId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", listId.toString())
                        .param("title", "New Title")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }

    @Test
    void updateWishlist_serviceThrowsOnUpdate_returnsEditView() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        WishlistDTO wishlist = wishlist(listId, userId, "My List");
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(currentUserService.requireAccessToken(any())).thenReturn("token");
        when(wishlistService.getWishlist(listId)).thenReturn(wishlist);
        doThrow(new RemoteServiceException("down")).when(wishlistService).updateWishlist(any(), any());

        mockMvc.perform(post("/wishlist/{listId}/edit", listId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", listId.toString())
                        .param("title", "Updated Title")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlists/editList"));
    }

    private WishlistDTO wishlist(UUID id, UUID ownerId, String title) {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(id);
        dto.setOwnerId(ownerId);
        dto.setTitle(title);
        return dto;
    }
}
