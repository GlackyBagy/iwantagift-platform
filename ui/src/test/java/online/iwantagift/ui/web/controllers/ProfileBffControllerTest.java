package online.iwantagift.ui.web.controllers;

import online.iwantagift.ui.models.dto.profile.ProfileDTO;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.ProfileService;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import online.iwantagift.ui.web.controllers.bff.ProfileBffController;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileBffController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileBffControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrentUserService currentUserService;
    @MockitoBean
    private ProfileService profileService;

    @Test
    void currentProfile_returnsProfileJson() throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(currentUserService.email(any())).thenReturn("user@example.com");
        when(profileService.getProfile(userId))
                .thenReturn(new ProfileDTO(userId, "GiftUser", "Loves gadgets", true));
        when(profileService.avatarUrl(userId)).thenReturn("http://profile/api/v1/profile/" + userId + "/avatar");

        mockMvc.perform(get("/api/profile")
                        .principal(authentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.nickname").value("GiftUser"))
                .andExpect(jsonPath("$.description").value("Loves gadgets"))
                .andExpect(jsonPath("$.avatarUrl").value("http://profile/api/v1/profile/" + userId + "/avatar"));
    }

    @Test
    void currentProfile_missingDescriptionAndAvatar_usesDefaults() throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(userId);
        when(currentUserService.email(any())).thenReturn("user@example.com");
        when(profileService.getProfile(userId))
                .thenReturn(new ProfileDTO(userId, "New user", null, false));

        mockMvc.perform(get("/api/profile")
                        .principal(authentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Profile description is not set yet."))
                .andExpect(jsonPath("$.avatarUrl").value("/img/logo_load_error.png"));
    }

    @Test
    void profileById_returnsPublicProfileWithoutEmail() throws Exception {
        UUID ownerId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(profileService.getProfile(ownerId))
                .thenReturn(new ProfileDTO(ownerId, "Owner", "Bio", true));
        when(profileService.avatarUrl(ownerId)).thenReturn("http://profile/avatar/" + ownerId);

        mockMvc.perform(get("/api/profile/{id}", ownerId)
                        .principal(authentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Owner"))
                .andExpect(jsonPath("$.description").value("Bio"))
                .andExpect(jsonPath("$.avatarUrl").value("http://profile/avatar/" + ownerId))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void profileById_serviceUnavailable_returns503() throws Exception {
        UUID ownerId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        when(profileService.getProfile(ownerId))
                .thenThrow(new RemoteServiceException("boom"));

        mockMvc.perform(get("/api/profile/{id}", ownerId)
                        .principal(authentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void currentProfile_serviceUnavailable_returns503() throws Exception {
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(UUID.randomUUID());
        when(profileService.getProfile(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "down"));

        mockMvc.perform(get("/api/profile")
                        .principal(authentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void currentProfile_remoteFailure_mappedTo503() throws Exception {
        Authentication authentication = mock(Authentication.class);

        when(currentUserService.requireUserId(any())).thenReturn(UUID.randomUUID());
        when(profileService.getProfile(any()))
                .thenThrow(new RemoteServiceException("boom"));

        mockMvc.perform(get("/api/profile")
                        .principal(authentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable());
    }
}
