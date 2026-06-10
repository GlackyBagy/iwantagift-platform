package online.iwantagift.ui.web.advice;

import online.iwantagift.ui.models.mappers.WishMapper;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.WishService;
import online.iwantagift.ui.services.WishlistService;
import online.iwantagift.ui.web.controllers.wl.WishController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Verifies that a downstream service being unreachable (connection refused / timeout) surfaces as
 * the 503 page rather than a generic 500, using a real MVC controller path.
 */
@WebMvcTest(WishController.class)
@Import(RemoteFailureControllerAdvice.class)
@AutoConfigureMockMvc(addFilters = false)
public class RemoteFailureControllerAdviceTest {
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

    @Test
    void serviceUnreachable_rendersServiceUnavailablePage() throws Exception {
        UUID wishId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(UUID.randomUUID());
        when(wishService.findWishById(wishId))
                .thenThrow(new ResourceAccessException("Connection refused"));

        mockMvc.perform(get("/wish/{wishId}/edit", wishId)
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isServiceUnavailable())
                .andExpect(view().name("error/503"));
    }
}
