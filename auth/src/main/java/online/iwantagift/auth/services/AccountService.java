package online.iwantagift.auth.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.repositories.AccountRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService  {
    private final AccountRepository accountRepository;

    public void save(Account account) {
        accountRepository.save(account);
    }
}
