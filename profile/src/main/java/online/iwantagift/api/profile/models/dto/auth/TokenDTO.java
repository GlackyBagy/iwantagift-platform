package online.iwantagift.api.profile.models.dto.auth;

import lombok.Data;

@Data
public class TokenDTO {
    private String jwtToken;
    private String refreshToken;
}
