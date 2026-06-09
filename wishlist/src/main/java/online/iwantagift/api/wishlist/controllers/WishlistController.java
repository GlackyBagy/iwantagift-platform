package online.iwantagift.api.wishlist.controllers;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.WishlistDTO;
import online.iwantagift.api.wishlist.models.dto.abstracts.ValidationGroups;
import online.iwantagift.api.wishlist.models.mapping.WishMapper;
import online.iwantagift.api.wishlist.models.mapping.WishlistMapper;
import online.iwantagift.api.wishlist.services.WishlistService;
import online.iwantagift.api.wishlist.util.exceptions.ValidationFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/list")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService listService;
    private final WishlistMapper wishlistMapper;
    private final WishMapper wishMapper;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WishlistDTO getWishlist(@PathVariable UUID id) {
        return wishlistMapper.toDTO(
                listService.findByIdOrThrow(id), wishMapper
        );
    }

    @GetMapping("/userLists")
    @ResponseStatus(HttpStatus.OK)
    public List<WishlistDTO> getUserLists(@RequestParam UUID userId) {
        return listService.findAllByOwnerId(userId).stream()
                .map(x -> wishlistMapper.toDTO(x, wishMapper))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> createWishlist(@RequestBody @Validated(ValidationGroups.Create.class)
                                            WishlistDTO dto, BindingResult bindingResult,
                                            Authentication authentication) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());

        UUID requesterId = UUID.fromString(authentication.getName());

        return Collections.singletonMap("id",
                listService.create(requesterId, wishlistMapper.toEntity(dto)));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void patchWishlist(@RequestBody @Validated(ValidationGroups.Patch.class)
                              WishlistDTO dto, BindingResult bindingResult,
                              Authentication authentication) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());

        UUID requesterId = UUID.fromString(authentication.getName());

        listService.patch(requesterId, dto);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void putWishlist(@RequestBody @Validated(ValidationGroups.Put.class)
                            WishlistDTO dto, BindingResult bindingResult,
                            Authentication authentication) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());

        UUID requesterId = UUID.fromString(authentication.getName());

        listService.put(requesterId, dto);
    }
}
