package online.iwantagift.api.wishlist.controllers;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.messaging.kafka.NewWishProducer;
import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.dto.abstracts.ValidationGroups;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.events.WishCreatedEvent;
import online.iwantagift.api.wishlist.models.mapping.WishMapper;
import online.iwantagift.api.wishlist.services.WishService;
import online.iwantagift.api.wishlist.services.WishlistService;
import online.iwantagift.api.wishlist.util.exceptions.ValidationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wish")
@RequiredArgsConstructor
public class WishController {
    private static final Logger log = LoggerFactory.getLogger(WishController.class);
    private final WishService wishService;
    private final WishMapper wishMapper;
    private final WishlistService wishlistService;
    private final NewWishProducer wishProducer;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WishDTO getWish(@PathVariable UUID id) {
        Wish wish = wishService.findByIdOrThrow(id);
        return wishMapper.toDTO(wish);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> createWish(@RequestBody @Validated(ValidationGroups.Create.class) WishDTO dto,
                                        BindingResult bindingResult,
                                        Authentication authentication) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());

        UUID requesterId = UUID.fromString(authentication.getName());

        log.info("WishController::createWish, DTO got: {}", dto.toString());

        Wish wish = wishMapper.toEntity(dto, requesterId, wishlistService);
        UUID wishId = wishService.create(requesterId, wish);
        wishProducer.send(new WishCreatedEvent(
                wishId,
                requesterId,
                dto.getTitle(),
                dto.getDescription(),
                dto.getUrl(),
                wish.getWishlist() == null ? dto.getWishListId() : wish.getWishlist().getId()
        ));

        return Collections.singletonMap("id", wishId);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void patchWish(@RequestBody @Validated({ValidationGroups.Patch.class}) WishDTO dto,
                          BindingResult bindingResult,
                          Authentication authentication) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());

        UUID requesterId = UUID.fromString(authentication.getName());

        wishService.patch(requesterId, dto);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void putWish(@RequestBody @Validated({ValidationGroups.Put.class}) WishDTO dto,
                        BindingResult bindingResult,
                        Authentication authentication) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());

        UUID requesterId = UUID.fromString(authentication.getName());

        wishService.put(requesterId, dto);
    }
}
