package online.iwantagift.auth.models.dto;

import online.iwantagift.auth.models.entities.Account;

import java.util.UUID;

public record AccountEventDTO(UUID id, String nickname, String email) {

    public static AccountEventDTO from(Account account) {
        return new AccountEventDTO(account.getId(), account.getNickname(), account.getEmail());
    }
}
