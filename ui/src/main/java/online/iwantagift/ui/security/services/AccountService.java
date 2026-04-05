package online.iwantagift.ui.security.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.models.entities.Account;
import online.iwantagift.ui.security.repositories.AccountRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;

    public void save(Account account) {
        accountRepository.save(account);
    }

    public Optional<String> passwordHashByEmail(String email) {
        return accountRepository.findPasswordHashByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return null;
    }
}
