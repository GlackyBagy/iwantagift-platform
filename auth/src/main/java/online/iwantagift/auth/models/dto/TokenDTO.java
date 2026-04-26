package online.iwantagift.auth.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import tools.jackson.databind.ObjectMapper;

@Data
@AllArgsConstructor
public class TokenDTO {
    @JsonIgnore
    private static final ObjectMapper mapper = new ObjectMapper();

    private String jwtToken;
    private String refreshToken;

    @Override
    public String toString() {
        return mapper.writeValueAsString(this);
    }
}
