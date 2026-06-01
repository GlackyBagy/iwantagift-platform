package online.iwantagift.ui.models.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import online.iwantagift.ui.models.validation.WishValidationGroups;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

@Data
public class WishPayload {
    @Null(groups = WishValidationGroups.CreateWish.class)
    private UUID id;

    @NotBlank(groups = {WishValidationGroups.CreateWish.class, WishValidationGroups.UpdateWish.class})
    @Length(min = 1, max = 255, groups = {WishValidationGroups.CreateWish.class, WishValidationGroups.UpdateWish.class})
    private String title;

    @Length(max = 1000, groups = {WishValidationGroups.CreateWish.class, WishValidationGroups.UpdateWish.class})
    private String description;

    @Length(max = 1000, groups = {WishValidationGroups.CreateWish.class, WishValidationGroups.UpdateWish.class})
    @URL(groups = {WishValidationGroups.CreateWish.class, WishValidationGroups.UpdateWish.class})
    @NotBlank(groups = {WishValidationGroups.CreateWish.class, WishValidationGroups.UpdateWish.class})
    private String url;

    @JsonProperty("wish_list_id")
    @NotNull(groups = {WishValidationGroups.CreateWish.class, WishValidationGroups.UpdateWish.class})
    private UUID wishlistId;

    @JsonProperty("default_price")
    @Min(value = 0, groups = {WishValidationGroups.CreateWish.class, WishValidationGroups.UpdateWish.class})
    private Integer defaultPrice;

    @JsonProperty("owner_id")
    private UUID ownerId;
}
