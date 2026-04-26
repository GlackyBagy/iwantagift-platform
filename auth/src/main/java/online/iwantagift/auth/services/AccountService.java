package online.iwantagift.auth.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.repositories.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService  {
    private final AccountRepository accountRepository;

    public void save(Account account) {
        accountRepository.save(account);
    }

    public Optional<UUID> userIdByEmail(String email) {
        return accountRepository.findIdByEmail(email);
    }

    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id);
    }
}
