package online.iwantagift.api.wishlist.models.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.abstracts.WishWriteDTO;

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
public class WishPatchDTO extends WishWriteDTO {
}