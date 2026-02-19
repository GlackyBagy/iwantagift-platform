package online.iwantagift.api.wishlist.models.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import online.iwantagift.api.wishlist.models.dto.abstracts.WishWriteDTO;

/**
 * Data Transfer Object for creating a new wish item.
 *
 * <p>This DTO is used for write operations (POST) and should not contain
 * system-managed fields such as {@code id} or {@code createdAt}.</p>
 *
 * <p>
 * The {@code wishListId} field specifies the target wishlist where
 * the wish item will be created. If {@code wishListId} is {@code null},
 * the wish item will be automatically assigned to the user's default wishlist.
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString(callSuper = true)
public class WishCreateDTO extends WishWriteDTO {
}