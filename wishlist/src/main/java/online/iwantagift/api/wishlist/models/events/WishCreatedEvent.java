package online.iwantagift.api.wishlist.models.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record WishCreatedEvent(
        @JsonProperty("wish_id")
        UUID wishId,

        @JsonProperty("owner_id")
        UUID ownerId,

        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("url")
        String url,

        @JsonProperty("wish_list_id")
        UUID wishListId
) {
}
