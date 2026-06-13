package online.iwantagift.api.wishlist.models.mapping;

import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface WishMapper {

    @Mapping(target = "wishlist", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    Wish toEntity(WishDTO dto, @Context UUID ownerId, @Context WishlistService wlService);

    @AfterMapping
    default void fillWishlist(WishDTO dto,
                              @MappingTarget Wish.WishBuilder wish,
                              @Context UUID ownerId,
                              @Context WishlistService wlService) {
        wish.wishlist(
                wlService.findByIdOrDefaultIfNull(dto.getWishListId(), ownerId)
        );
    }

    @Mapping(target = "wishListId", expression = "java(entity.getWishlist().getId())")
    WishDTO toDTO(Wish entity);
}
