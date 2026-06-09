package online.iwantagift.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ConfigurationProperties("iwag")
@Getter
public class IwagProperties {

    private final Map<String, ServiceProperties> services = new HashMap<>();

    /** Base URL of the ui service (used for links to signup etc.). */
    @Setter
    private String uiBaseUrl;

    @Getter
    @Setter
    public static class ServiceProperties {
        private Set<String> kafkaTopics = Set.of();
    }
}
