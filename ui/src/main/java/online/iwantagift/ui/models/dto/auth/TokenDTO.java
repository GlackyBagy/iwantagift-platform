package online.iwantagift.ui.models.dto.auth;

import lombok.Data;

@Data
public class TokenDTO {
    private String jwtToken;
    private String refreshToken;
}
