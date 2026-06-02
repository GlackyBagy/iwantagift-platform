package online.iwantagift.ui.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.IwagProperties;
import online.iwantagift.ui.models.payloads.ProfilePayload;
import online.iwantagift.ui.util.exceptions.HttpErrorHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final IwagProperties iwagProperties;
    private RestClient restClient;

    @PostConstruct
    public void init() {
        restClient = RestClient.builder()
                .baseUrl(iwagProperties.getRequiredService("profile").getBaseUrl())
                .build();
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
