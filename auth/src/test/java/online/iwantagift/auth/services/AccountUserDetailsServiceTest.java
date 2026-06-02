package online.iwantagift.auth.services;

import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountUserDetailsServiceTest {

    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    AccountUserDetailsService service;

    @Test
    void loadUserByUsername_found_returnsUserDetailsWithEmailAndHash() {
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .email("alice@example.com")
                .passwordHash("$2a$hash")
                .build();
        when(accountRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(account));

        UserDetails userDetails = service.loadUserByUsername("alice@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("alice@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$hash");
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(accountRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatExceptionOfType(UsernameNotFoundException.class)
                .isThrownBy(() -> service.loadUserByUsername("unknown@example.com"));
    }
}
