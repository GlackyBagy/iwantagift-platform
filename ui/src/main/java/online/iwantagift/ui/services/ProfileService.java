package online.iwantagift.ui.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.IwagProperties;
import online.iwantagift.ui.models.dto.profile.ProfileDTO;
import online.iwantagift.ui.models.payloads.ProfilePayload;
import online.iwantagift.ui.util.exceptions.HttpErrorHandler;
import online.iwantagift.ui.util.exceptions.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.lang.Contract;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final IwagProperties iwagProperties;
    private RestClient restClient;
    private String profileBaseUrl;

    @PostConstruct
    public void init() {
        profileBaseUrl = iwagProperties.getRequiredService("profile").getBaseUrl();
        restClient = RestClient.builder()
                .baseUrl(profileBaseUrl)
                .build();
    }

    @Contract("_ -> !null")
    public ProfileDTO getProfile(UUID profileId) {
        ProfileDTO res = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/profile")
                        .queryParam("profileId", profileId)
                        .build())
                .retrieve()
                .onStatus(new HttpErrorHandler())
                .body(ProfileDTO.class);

        if (res == null)
            throw new NotFoundException("Got null profile with id: " + profileId);

        return res;
    }

    public String avatarUrl(UUID profileId) {
        return profileBaseUrl + "/api/v1/profile/" + profileId + "/avatar";
    }

    public void updateProfile(ProfilePayload payload, String accessToken) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        addTextPart(body, "nickname", payload.getNickname());
        addTextPart(body, "description", payload.getDescription());
        addProfilePhoto(body, payload.getProfilePhoto());

        restClient
                .post()
                .uri("/api/v1/edit")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body.build())
                .retrieve()
                .onStatus(new HttpErrorHandler())
                .toBodilessEntity();
    }

    private void addTextPart(MultipartBodyBuilder body, String name, String value) {
        if (value != null)
            body.part(name, value);
    }

    private void addProfilePhoto(MultipartBodyBuilder body, MultipartFile profilePhoto) {
        if (profilePhoto == null || profilePhoto.isEmpty()) {
            return;
        }

        String contentType = profilePhoto.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : profilePhoto.getContentType();

        String fileName = profilePhoto.getOriginalFilename() == null
                ? Instant.now(Clock.system(ZoneId.of("UTC"))).toString()
                : profilePhoto.getOriginalFilename();

        body.part("profilePhoto", profilePhoto.getResource())
                .filename(fileName)
                .contentType(MediaType.parseMediaType(contentType));
    }
}
