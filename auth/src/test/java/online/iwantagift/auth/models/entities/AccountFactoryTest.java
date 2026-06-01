package online.iwantagift.auth.models.entities;

import online.iwantagift.auth.models.dto.CredentialsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link AccountFactory#create(CredentialsDTO)} contract:
 * – DTO fields are mapped to Account fields,
 * – password is encoded via PasswordEncoder (not stored in plain text),
 * – null DTO throws NullPointerException.
 */
@ExtendWith(MockitoExtension.class)
class AccountFactoryTest {

    @Mock
    PasswordEncoder passwordEncoder;

    AccountFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AccountFactory(passwordEncoder);
    }

    @Test
    void create_mapsAllFieldsFromDto() {
        CredentialsDTO dto = new CredentialsDTO();
        dto.setNickname("alice");
        dto.setEmail("alice@example.com");
        dto.setPassword("password1");
        when(passwordEncoder.encode("password1")).thenReturn("hashed");

        Account account = factory.create(dto);

        assertThat(account.getNickname()).isEqualTo("alice");
        assertThat(account.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void create_storesEncodedPassword_notRawPassword() {
        CredentialsDTO dto = new CredentialsDTO();
        dto.setNickname("bob");
        dto.setEmail("bob@example.com");
        dto.setPassword("rawpass");
        when(passwordEncoder.encode("rawpass")).thenReturn("$2a$encoded");

        Account account = factory.create(dto);

        assertThat(account.getPasswordHash()).isEqualTo("$2a$encoded");
        assertThat(account.getPasswordHash()).isNotEqualTo("rawpass");
    }

    @Test
    void create_nullDto_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> factory.create(null));
    }
}
