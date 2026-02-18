package online.iwantagift.api.wishlist.models.mapping;

import lombok.NoArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistCreateDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistDTO;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
@NoArgsConstructor
public abstract class WishlistMapper {
    protected WishlistService wlService;
    protected WishMapper wishMapper;

    @Autowired
    protected WishlistMapper(WishlistService wlService) {
        this.wlService = wlService;
    }

    @Mapping(target = "wishes",
            expression = "java(" +
                    "entity.getWishes().stream().map(wishMapper::toDTO).toList()" +
                    ")")
    public abstract WishlistDTO toDTO(Wishlist entity);

    public abstract Wishlist toEntity(WishlistCreateDTO dto);
}
