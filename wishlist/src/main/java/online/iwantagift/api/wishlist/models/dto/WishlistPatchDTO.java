package online.iwantagift.api.wishlist.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.abstracts.WishlistWriteDTO;

/**
 * Data Transfer Object for partially updating an existing wishlist.
 *
 * <p>This DTO is used in PATCH operations. All fields are optional.
 * Only non-null fields will be applied to the target wishlist.</p>
 *
 * <p>Fields omitted or set to {@code null} will not be modified.</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistPatchDTO extends WishlistWriteDTO {
}
