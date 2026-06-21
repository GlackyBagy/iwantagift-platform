package online.iwantagift.api.profile.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProfileDTO {
    private UUID id;
    private String nickname;
    private String description;
    private boolean hasAvatar;
}
