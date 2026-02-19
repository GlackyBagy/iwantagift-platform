package online.iwantagift.api.wishlist.controllers;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.exceptions.ValidationFailedException;
import online.iwantagift.api.wishlist.models.dto.WishCreateDTO;
import online.iwantagift.api.wishlist.models.dto.WishDTO;
import online.iwantagift.api.wishlist.models.dto.WishPatchDTO;
import online.iwantagift.api.wishlist.models.dto.WishPutDTO;
import online.iwantagift.api.wishlist.models.dto.abstracts.ValidationGroups;
import online.iwantagift.api.wishlist.models.entities.Wish;
import online.iwantagift.api.wishlist.models.mapping.WishMapper;
import online.iwantagift.api.wishlist.services.WishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
//    @PreAuthorize("hasRole(Authorized)")
    public Map<String, UUID> createWish(@RequestBody @Validated(ValidationGroups.Create.class) WishCreateDTO dto,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());

        return Collections.singletonMap("id",
                wishService.create(wishMapper.toEntity(dto)));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WishDTO getWish(@PathVariable UUID id) {
        Wish wish = wishService.findByIdOrThrow(id);
        return wishMapper.toDTO(wish);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
//    @PreAuthorize("@wishSecurity.canModifyWish(#dto, authentication)")
    public void patchWish(@RequestBody @Validated({ValidationGroups.Patch.class}) WishPatchDTO dto,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());
        wishService.update(dto);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
//    @PreAuthorize("@wishSecurity.canModifyWish(#dto, authentication)")
    public void putWish(@RequestBody @Validated({ValidationGroups.Put.class}) WishPutDTO dto,
                        BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new ValidationFailedException(bindingResult.getFieldErrors());
        wishService.update(dto);
    }
}
