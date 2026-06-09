package online.iwantagift.api.wishlist.models.mapping;

import online.iwantagift.api.wishlist.models.dto.WishlistDTO;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    @Mapping(target = "wishes", ignore = true)
    WishlistDTO toDTO(Wishlist entity,
                      @Context WishMapper wishMapper);

    @AfterMapping
    default void fillWishes(Wishlist entity,
                            @MappingTarget WishlistDTO.WishlistDTOBuilder dto,
                            @Context WishMapper wishMapper) {
        if (entity == null || entity.getWishes() == null) {
            dto.wishes(java.util.List.of());
            return;
        }
        dto.wishes(entity.getWishes().stream()
                .map(wishMapper::toDTO)
                .toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "wishes", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    Wishlist toEntity(WishlistDTO dto);
}
