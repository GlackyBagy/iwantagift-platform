package online.iwantagift.api.wishlist.controllers;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.exceptions.ValidationFailedException;
import online.iwantagift.api.wishlist.models.dto.abstracts.ValidationGroups;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistCreateDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistPatchDTO;
import online.iwantagift.api.wishlist.models.dto.wl.WishlistPutDTO;
import online.iwantagift.api.wishlist.models.mapping.WishMapper;
import online.iwantagift.api.wishlist.models.mapping.WishlistMapper;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> createWishlist(@RequestBody @Validated(ValidationGroups.Create.class)
                                            WishlistCreateDTO dto,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());

        return Collections.singletonMap("id",
                listService.create(wishlistMapper.toEntity(dto)));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void patchWishlist(@RequestBody @Validated(ValidationGroups.Patch.class)
                              WishlistPatchDTO dto,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());
        listService.update(dto);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void putWishlist(@RequestBody @Validated(ValidationGroups.Put.class)
                            WishlistPutDTO dto,
                            BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());
        listService.update(dto);
    }
}
