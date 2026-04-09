package online.iwantagift.ui.models.dto;

import lombok.Data;

@Data
public class TokenDTO {
    private String jwtToken;
    private String refreshToken;
}
