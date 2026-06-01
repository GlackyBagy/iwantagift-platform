package online.iwantagift.auth.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sha256EncoderTest {

    @Test
    void encode_sameInput_returnsSameHash() {
        String input = "someToken123";
        assertThat(Sha256Encoder.encode(input)).isEqualTo(Sha256Encoder.encode(input));
    }

    @Test
    void encode_differentInputs_returnDifferentHashes() {
        assertThat(Sha256Encoder.encode("token-a")).isNotEqualTo(Sha256Encoder.encode("token-b"));
    }

    @Test
    void encode_returnsLowerHexString_ofLength64() {
        String hash = Sha256Encoder.encode("test");
        assertThat(hash).hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    void encode_emptyString_doesNotThrow() {
        assertThat(Sha256Encoder.encode("")).isNotNull();
    }
}
