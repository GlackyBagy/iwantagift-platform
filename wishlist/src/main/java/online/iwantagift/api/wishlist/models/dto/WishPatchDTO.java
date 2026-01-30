package online.iwantagift.api.wishlist.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.abstracts.ValidationGroups;
import online.iwantagift.api.wishlist.models.dto.abstracts.WishWriteDTO;

import java.util.UUID;

/**
 * Data Transfer Object for patching an existing wish item.
 *
 * <p>This DTO is used in PATCH operations.
 *  All fields are optional.
 * Only non-null fields will be applied to the target wish object.</p>
 *
 * <p>Fields omitted or set to {@code null} will not be modified.</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishPatchDTO extends WishWriteDTO {
    /**
     * Unique identifier of the wishlist.
     */
    @JsonProperty("id")
    @NotNull(groups = ValidationGroups.Patch.class)
    @NotEmpty(groups = ValidationGroups.Patch.class)
    private UUID id;
}