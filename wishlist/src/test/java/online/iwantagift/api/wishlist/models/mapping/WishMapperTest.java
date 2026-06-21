package online.iwantagift.api.wishlist.models.mapping;

import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishMapperTest {

    @Mock
    private WishlistService wlService;

    private final WishMapper mapper = new WishMapperImpl();

    @Test
    void toEntity_resolvesWishlistViaService_andMapsFields() {
        UUID ownerId = UUID.randomUUID();
        UUID wishListId = UUID.randomUUID();

        Wishlist wishlist = new Wishlist();
        wishlist.setId(wishListId);
        wishlist.setOwnerId(ownerId);

        when(wlService.findByIdOrDefaultIfNull(wishListId, ownerId)).thenReturn(wishlist);

        WishDTO dto = new WishDTO();
        dto.setWishListId(wishListId);
        dto.setTitle("PS5");
        dto.setDescription("Slim");
        dto.setUrl("https://example.com/ps5");

        Wish entity = mapper.toEntity(dto, ownerId, wlService);

        assertNotNull(entity);
        assertEquals("PS5", entity.getTitle());
        assertEquals("Slim", entity.getDescription());
        assertEquals("https://example.com/ps5", entity.getUrl());
        assertNull(entity.getOwnerId());
        assertSame(wishlist, entity.getWishlist());

        verify(wlService).findByIdOrDefaultIfNull(wishListId, ownerId);
        verifyNoMoreInteractions(wlService);
    }

    @Test
    void toEntity_whenWishlistIdIsNull_setsDefault() {
        UUID ownerId = UUID.randomUUID();

        Wishlist defaultList = new Wishlist();
        defaultList.setOwnerId(ownerId);

        WishDTO dto = new WishDTO();
        dto.setTitle("PS5");
        dto.setUrl("https://example.com/ps5");
        dto.setWishListId(null);

        when(wlService.findByIdOrDefaultIfNull(null, ownerId)).thenReturn(defaultList);

        Wish entity = mapper.toEntity(dto, ownerId, wlService);

        assertEquals(defaultList, entity.getWishlist());
        verify(wlService).findByIdOrDefaultIfNull(dto.getWishListId(), ownerId);
        verifyNoMoreInteractions(wlService);
    }

    @Test
    void toDTO_setsWishListIdFromEntityWishlist_andMapsFields() {
        UUID wishId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID wishListId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Wishlist wishlist = new Wishlist();
        wishlist.setId(wishListId);

        Wish entity = Wish.builder()
                .id(wishId)
                .title("AirPods")
                .description("Pro 2")
                .url("https://example.com/airpods")
                .createdAt(createdAt)
                .wishlist(wishlist)
                .ownerId(ownerId)
                .build();

        WishDTO dto = mapper.toDTO(entity);

        assertNotNull(dto);
        assertEquals(wishId, dto.getId());
        assertEquals("AirPods", dto.getTitle());
        assertEquals("Pro 2", dto.getDescription());
        assertEquals("https://example.com/airpods", dto.getUrl());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(wishListId, dto.getWishListId());

        verifyNoInteractions(wlService);
    }

    @Test
    void toDTO_whenWishlistIsNull_throwsNpe_dueToExpression() {
        Wish entity = Wish.builder()
                .id(UUID.randomUUID())
                .title("X")
                .wishlist(null)
                .build();

        assertThrows(NullPointerException.class, () -> mapper.toDTO(entity));
    }
}
