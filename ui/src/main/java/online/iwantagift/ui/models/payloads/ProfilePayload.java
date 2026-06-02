package online.iwantagift.ui.models.payloads;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfilePayload {
    @Size(min = 1, max = 64, message = "Nickname must be between 1 and 64 characters")
    private String nickname;

    @Size(max = 500, message = "Description must not be longer than 500 characters")
    private String description;

    private MultipartFile profilePhoto;
}
