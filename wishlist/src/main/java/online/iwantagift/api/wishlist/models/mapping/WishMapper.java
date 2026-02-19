package online.iwantagift.api.wishlist.models.mapping;

import online.iwantagift.api.wishlist.models.dto.WishCreateDTO;
import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface WishMapper {

    @Mapping(target = "wishlist", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Wish toEntity(WishCreateDTO dto, @Context WishlistService wlService);

    @AfterMapping
    default void fillWishlist(WishCreateDTO dto,
                              @MappingTarget Wish.WishBuilder wish,
                              @Context WishlistService wlService) {
        wish.wishlist(wlService.findById(dto.getWishListId()).orElse(null));
    }

    @Mapping(target = "wishListId", expression = "java(entity.getWishlist().getId())")
    WishDTO toDTO(Wish entity);
}
