package online.iwantagift.ui.models.mappers;

import online.iwantagift.ui.models.dto.wl.WishDTO;
import online.iwantagift.ui.models.payloads.WishPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishMapper {
    @Mapping(target = "wishlistId", source = "wishListId")
    WishPayload toWishPayload(WishDTO dto);

    @Mapping(target = "wishListId", source = "wishlistId")
    WishDTO toWishDTO(WishPayload payload);
}
