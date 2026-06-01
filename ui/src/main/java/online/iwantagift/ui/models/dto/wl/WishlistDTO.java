package online.iwantagift.ui.models.dto.wl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
public class WishlistDTO {
    private UUID id;
    private String title;
    private String description;
    private Instant createdAt;

    @JsonProperty("owner_id")
    private UUID ownerId;

    private List<WishDTO> wishes = Collections.emptyList();
}
