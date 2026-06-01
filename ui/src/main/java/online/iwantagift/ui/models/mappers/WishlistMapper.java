package online.iwantagift.ui.models.mappers;

import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.models.payloads.WishlistPayload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WishlistMapper {
    WishlistPayload toWishlistPayload(WishlistDTO dto);

    WishlistDTO toWishlistDTO(WishlistPayload payload);
}
