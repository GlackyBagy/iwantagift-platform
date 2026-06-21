package online.iwantagift.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.*;

@ConfigurationProperties("iwag")
@Getter
public class IwagProperties {

    private final Map<String, ServiceProperties> services = new HashMap<>();
    private final Set<String> usingKafkaTopics = new HashSet<>();

    public ServiceProperties requireService(String name) {
        return Objects.requireNonNull(services.get(name));
    }

    @Setter
    public static class ServiceProperties {
        private String clientSecret;
        private URL baseUrl;

        public URL requireBaseUrl() {
            return Objects.requireNonNull(baseUrl);
        }

        public String requireClientSecret() {
            if (StringUtils.hasText(clientSecret) && clientSecret.length() >= 8)
                return clientSecret;
            throw new IllegalArgumentException("Invalid configuration: too short client secret");
        }
    }
}
