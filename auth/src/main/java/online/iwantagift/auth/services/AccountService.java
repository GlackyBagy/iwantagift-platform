package online.iwantagift.auth.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.repositories.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public void save(Account account) {
        accountRepository.save(account);
    }

    @Transactional
    public boolean updateEmail(String oldEmail, String newEmail) {
        Optional<Account> account = accountRepository.findByEmail(oldEmail);
        if (account.isEmpty())
            return false;

        account.get().setEmail(newEmail);
        account.get().setEmailVerified(true);
        return true;
    }

    @Transactional
    public boolean verifyEmail(String email) {
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isEmpty())
            return false;

        account.get().setEmailVerified(true);
        return true;
    }

    @Transactional
    public boolean updatePassword(String email, String passwordHash){
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isEmpty())
            return false;

        account.get().setPasswordHash(passwordHash);
        return true;
    }

    public Optional<UUID> userIdByEmail(String email) {
        return accountRepository.findIdByEmail(email);
    }

    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id);
    }
}
