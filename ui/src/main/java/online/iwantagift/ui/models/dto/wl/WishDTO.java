package online.iwantagift.ui.models.dto.wl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class WishDTO {
    private UUID id;
    private String title;
    private String description;
    private String url;
    private Instant createdAt;

    @JsonProperty("wish_list_id")
    private UUID wishListId;

    @JsonProperty("owner_id")
    private UUID ownerId;
}
