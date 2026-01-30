package online.iwantagift.api.wishlist.models.dto.abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class WishlistWriteDTO {
    /**
     * New display title of the wishlist.
     *
     * <p>If {@code null}, the title will not be changed.</p>
     */
    @JsonProperty("title")
    @Length(min = 1, groups = ValidationGroups.Patch.class)
    @NotBlank(groups = ValidationGroups.Create.class)
    private String title;

    /**
     * New description of the wishlist.
     *
     * <p>If {@code null}, the description will not be changed.</p>
     */
    @JsonProperty("description")
    private String description;
}
