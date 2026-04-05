package online.iwantagift.ui.security.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.models.entities.Account;
import online.iwantagift.ui.security.repositories.AccountRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService  {
    private final AccountRepository accountRepository;

    public void save(Account account) {
        accountRepository.save(account);
    }
}
