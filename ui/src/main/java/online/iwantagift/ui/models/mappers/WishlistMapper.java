package online.iwantagift.ui.models.mappers;

import online.iwantagift.ui.models.dto.wl.WishlistDTO;
import online.iwantagift.ui.models.payloads.WishlistPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistMapper {
    WishlistPayload toWishlistPayload(WishlistDTO dto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "wishes", ignore = true)
    WishlistDTO toWishlistDTO(WishlistPayload payload);
}
