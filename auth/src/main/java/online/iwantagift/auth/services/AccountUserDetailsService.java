package online.iwantagift.auth.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.repositories.AccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    public UserDetails  loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("Not found user with email: " + username));
        return new User(account.getEmail(), account.getPasswordHash(), Collections.emptyList());
    }
}
