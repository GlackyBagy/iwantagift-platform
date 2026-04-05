package online.iwantagift.ui.models.entities;

import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.models.dto.CredentialsDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class AccountFactory {
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new {@link Account} instance from the given credentials.
     *
     * <p><b>Contract:</b> the provided {@link CredentialsDTO} is assumed to be valid.
     * This method does not perform validation of the DTO fields and expects that
     * all required constraints have already been checked before invocation.
     *
     * @param credentialsDTO a valid credentials DTO containing the source data
     *                       for account creation
     * @return a new {@link Account} populated from the given DTO
     * @throws NullPointerException if {@code credentialsDTO} is {@code null}
     */
    public Account create(CredentialsDTO credentialsDTO) {
        return Account.builder()
                .nickname(credentialsDTO.getNickname())
                .email(credentialsDTO.getEmail())
                .passwordHash(passwordEncoder.encode(credentialsDTO.getPassword()))
                .build();
    }
}