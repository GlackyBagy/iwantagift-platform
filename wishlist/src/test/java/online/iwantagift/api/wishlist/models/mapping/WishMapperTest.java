package online.iwantagift.api.wishlist.models.mapping;

import online.iwantagift.api.wishlist.models.dto.WishCreateDTO;
import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.junit.jupiter.api.BeforeEach;
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

    private WishMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WishMapperImpl();
        mapper.wlService = wlService;
    }

    @Test
    void toEntity_resolvesWishlistViaService_andMapsFields() {
        UUID wishListId = UUID.randomUUID();
        Wishlist wishlist = new Wishlist();
        wishlist.setId(wishListId);

        when(wlService.findByIdOrThrow(wishListId)).thenReturn(wishlist);

        WishCreateDTO dto = new WishCreateDTO();
        dto.setWishListId(wishListId);
        dto.setTitle("PS5");
        dto.setDescription("Slim");
        dto.setUrl("https://example.com/ps5");

        Wish entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("PS5", entity.getTitle());
        assertEquals("Slim", entity.getDescription());
        assertEquals("https://example.com/ps5", entity.getUrl());
        assertSame(wishlist, entity.getWishlist());

        verify(wlService, times(1)).findByIdOrThrow(wishListId);
    }

    @Test
    void toDTO_setsWishListIdFromEntityWishlist_andMapsFields() {
        UUID wishId = UUID.randomUUID();
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
