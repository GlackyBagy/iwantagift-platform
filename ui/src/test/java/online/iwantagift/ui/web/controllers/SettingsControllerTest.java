package online.iwantagift.ui.web.controllers;

import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.ProfileService;
import online.iwantagift.ui.web.controllers.settings.SettingsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(SettingsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SettingsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrentUserService currentUserService;
    @MockitoBean
    private ProfileService profileService;
    @Test
    void settings_returnsSettingsView() throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(currentUserService.email(any())).thenReturn("user@example.com");

        mockMvc.perform(get("/settings")
                        .principal(authentication)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/index"))
                .andExpect(model().attribute("email", "user@example.com"))
                .andExpect(model().attribute("nickname", "User"))
                .andExpect(model().attributeExists("profileDescription"))
                .andExpect(model().attribute("profileOwnerId", userId))
                .andExpect(model().attribute("profileAvatarUrl", "/img/logo_load_error.png"))
                .andExpect(model().attributeExists("settingsCss"));
    }

    @Test
    void updateProfile_withProfilePhoto_redirectsToSettings() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(currentUserService.requireAccessToken(any())).thenReturn("access-token");

        MockMultipartFile profilePhoto = new MockMultipartFile(
                "profilePhoto",
                "avatar.webp",
                "image/webp",
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/settings/profile")
                        .file(profilePhoto)
                        .principal(authentication)
                        .param("nickname", "GiftUser")
                        .param("description", "Profile description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings"));

        verify(profileService).updateProfile(any(), eq("access-token"));
    }
}
