package online.iwantagift.ui.models.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import online.iwantagift.ui.models.validation.WishlistValidationGroups;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Data
public class WishlistPayload {
    @Null(groups = WishlistValidationGroups.CreateWishlist.class)
    @NotNull(groups = WishlistValidationGroups.UpdateWishlist.class)
    private UUID id;

    @Length(max = 100, groups = {
            WishlistValidationGroups.CreateWishlist.class,
            WishlistValidationGroups.UpdateWishlist.class
    })
    @Length(min = 1, groups = {WishlistValidationGroups.CreateWishlist.class})
    @NotBlank(groups = {
            WishlistValidationGroups.CreateWishlist.class,
            WishlistValidationGroups.UpdateWishlist.class
    })
    private String title;

    @Length(max = 1000, groups = {
            WishlistValidationGroups.CreateWishlist.class,
            WishlistValidationGroups.UpdateWishlist.class
    })
    private String description;

    @JsonProperty("owner_id")
    private UUID ownerId;
}
