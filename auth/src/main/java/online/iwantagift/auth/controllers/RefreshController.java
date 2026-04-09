package online.iwantagift.auth.controllers;

import online.iwantagift.auth.models.dto.TokenDTO;
import org.springframework.web.bind.annotation.*;

@RestController
public class RefreshController {

    @GetMapping("/refresh")
    public TokenDTO refresh(@RequestParam String refreshToken) {
        return null; //todo
    }
}
