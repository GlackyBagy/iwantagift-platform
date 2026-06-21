package online.iwantagift.api.profile.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ConfigurationProperties("iwag")
@Getter
public class IwagProperties {

    /**
     * Map of IWAG services configuration.
     * Key is a service name (e.g. auth, backend, payment).
     */
    private final Map<String, ServiceProperties> services = new HashMap<>();

    public ServiceProperties getRequiredService(String name) {
        return Optional.ofNullable(services.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Unknown service: " + name));
    }

    @Getter
    @Setter
    public static class ServiceProperties {

        /**
         * Base URL of the service (without protocol if useHttps is configured).
         */
        private String url;

        /**
         * Whether HTTPS should be used when communicating with the service.
         */
        private boolean useHttps;

        /**
         * Kafka topics this service produces messages to.
         */
        private Set<String> kafkaProduceTopics;

        /**
         * Kafka topics this service consumes messages from.
         */
        private Set<String> kafkaConsumeTopics;

        public String getBaseUrl() {
            if (url == null || url.isBlank()) {
                return url;
            }

            if (url.startsWith("http://") || url.startsWith("https://")) {
                return url;
            }

            return (useHttps ? "https://" : "http://") + url;
        }
    }
}
