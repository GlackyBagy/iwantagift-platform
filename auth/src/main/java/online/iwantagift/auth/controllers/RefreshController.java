package online.iwantagift.auth.controllers;

import online.iwantagift.auth.models.dto.TokenDTO;
import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.services.AccountService;
import online.iwantagift.auth.services.JwtService;
import online.iwantagift.auth.services.RefreshTokenService;
import online.iwantagift.auth.util.exceptions.TokenNotFoundException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
public class RefreshController {

    private final RefreshTokenService refreshTokenService;
    private final AccountService accountService;
    private final JwtService jwtService;

    public RefreshController(RefreshTokenService refreshTokenService, AccountService accountService, JwtService jwtService) {
        this.refreshTokenService = refreshTokenService;
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    @GetMapping("/refresh") // todo move param to request body
    public TokenDTO refresh(@RequestParam String refreshToken) throws BadRequestException, TokenNotFoundException {
        if (refreshToken == null || refreshToken.isEmpty())
            throw new BadRequestException("Refresh token is empty");
        if (!refreshTokenService.isTokenValid(refreshToken))
            throw new BadRequestException("Invalid refresh token");

        Account account = accountService.findById(
                refreshTokenService.findUserIdByRefreshToken(refreshToken)
        ).orElseThrow(() -> new NoSuchElementException("Account doesn't exist"));

        String newRefreshToken = refreshTokenService.rotate(refreshToken);

        return new TokenDTO(
                jwtService.jwtFromUsername(account.getEmail()),
                newRefreshToken
        );
    }

    @ExceptionHandler({BadRequestException.class, TokenNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private String handleBadToken(Exception e) {
        return "message: " + e.getMessage();
    }
}
