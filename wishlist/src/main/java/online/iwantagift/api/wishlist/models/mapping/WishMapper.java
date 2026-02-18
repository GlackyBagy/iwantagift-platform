package online.iwantagift.api.wishlist.models.mapping;

import lombok.NoArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.WishCreateDTO;
import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
@NoArgsConstructor
public abstract class WishMapper {
    protected WishlistService wlService;

    @Autowired
    protected WishMapper(WishlistService wlService) {
        this.wlService = wlService;
    }

    @Mapping(target = "wishlist",
            expression = "java(wlService.findByIdOrThrow(dto.getWishListId()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract Wish toEntity(WishCreateDTO dto);

    @Mapping(target = "wishListId",
            expression = "java(entity.getWishlist().getId())")
    public abstract WishDTO toDTO(Wish entity);
}
