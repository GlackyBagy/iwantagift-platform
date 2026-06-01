package online.iwantagift.api.wishlist.models.mapping;

import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistCreateDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistDTO;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.entities.Wishlist;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistMapperTest {

    @Mock
    private WishlistService wlService;

    @Spy
    private WishMapper wishMapper = Mappers.getMapper(WishMapper.class);

    private final WishlistMapper mapper = Mappers.getMapper(WishlistMapper.class);

    @Test
    void toEntity_fromCreateDTO_mapsWritableFields() {
        WishlistCreateDTO dto = new WishlistCreateDTO();
        dto.setTitle("Birthday");
        dto.setDescription("Gift ideas");

        Wishlist entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("Birthday", entity.getTitle());
        assertEquals("Gift ideas", entity.getDescription());
        assertNull(entity.getId());
        assertNull(entity.getOwnerId());
        assertNull(entity.getCreatedAt());
        assertNotNull(entity.getWishes());
        assertTrue(entity.getWishes().isEmpty());
    }

    @Test
    void toDTO_mapsFields_andMapsWishesUsingWishMapper() {
        // given
        UUID wishlistId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-02-18T00:00:00Z");

        Wishlist wishlist = new Wishlist();
        wishlist.setId(wishlistId);
        wishlist.setTitle("My WL");
        wishlist.setDescription("Desc");
        wishlist.setCreatedAt(createdAt);
        wishlist.setOwnerId(ownerId);

        Wish w1 = Wish.builder()
                .id(UUID.randomUUID())
                .title("Wish 1")
                .description("D1")
                .url("https://example.com/1")
                .createdAt(Instant.parse("2026-02-01T10:00:00Z"))
                .wishlist(wishlist)
                .ownerId(ownerId)
                .build();

        Wish w2 = Wish.builder()
                .id(UUID.randomUUID())
                .title("Wish 2")
                .description("D2")
                .url("https://example.com/2")
                .createdAt(Instant.parse("2026-02-02T10:00:00Z"))
                .wishlist(wishlist)
                .ownerId(ownerId)
                .build();

        wishlist.setWishes(List.of(w1, w2));

        // when
        WishlistDTO dto = mapper.toDTO(wishlist, wishMapper);

        // then: top-level fields
        assertNotNull(dto);
        assertEquals(wishlistId, dto.getId());
        assertEquals("My WL", dto.getTitle());
        assertEquals("Desc", dto.getDescription());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(ownerId, dto.getOwnerId());

        // then: wishes list
        assertNotNull(dto.getWishes());
        assertEquals(2, dto.getWishes().size());

        WishDTO dto1 = dto.getWishes().getFirst();
        assertEquals(w1.getId(), dto1.getId());
        assertEquals(w1.getTitle(), dto1.getTitle());
        assertEquals(w1.getDescription(), dto1.getDescription());
        assertEquals(w1.getUrl(), dto1.getUrl());
        assertEquals(w1.getCreatedAt(), dto1.getCreatedAt());
        assertEquals(wishlistId, dto1.getWishListId());

        WishDTO dto2 = dto.getWishes().get(1);
        assertEquals(w2.getId(), dto2.getId());
        assertEquals(w2.getTitle(), dto2.getTitle());
        assertEquals(w2.getDescription(), dto2.getDescription());
        assertEquals(w2.getUrl(), dto2.getUrl());
        assertEquals(w2.getCreatedAt(), dto2.getCreatedAt());
        assertEquals(wishlistId, dto2.getWishListId());

        // and: verify WishMapper is actually used for each wish
        verify(wishMapper, times(1)).toDTO(w1);
        verify(wishMapper, times(1)).toDTO(w2);
        verifyNoMoreInteractions(wishMapper);
    }

    @Test
    void toDTO_whenWishesEmpty_returnsEmptyListNotNull() {
        // given
        Wishlist wishlist = new Wishlist();
        wishlist.setId(UUID.randomUUID());
        wishlist.setTitle("Empty WL");
        wishlist.setWishes(List.of()); // NOT null

        // when
        WishlistDTO dto = mapper.toDTO(wishlist, wishMapper);

        // then
        assertNotNull(dto);
        assertNotNull(dto.getWishes());
        assertTrue(dto.getWishes().isEmpty());

        verifyNoInteractions(wishMapper);
    }
}
