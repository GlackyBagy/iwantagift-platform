package online.iwantagift.ui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

/**
 * Shared HTTP client settings for outbound calls to downstream services.
 *
 * <p>Connect/read timeouts ensure a hung or unreachable downstream service fails fast instead of
 * holding a servlet thread indefinitely (which would eventually exhaust the thread pool and take
 * the whole ui down). On timeout the {@code RestClient} raises a
 * {@link org.springframework.web.client.ResourceAccessException}, which callers translate into a
 * degraded/unavailable response.
 *
 * <p>Values are tunable via {@code iwag.http.connect-timeout} / {@code iwag.http.read-timeout}.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public ClientHttpRequestFactory remoteServiceRequestFactory(
            @Value("${iwag.http.connect-timeout:2s}") Duration connectTimeout,
            @Value("${iwag.http.read-timeout:5s}") Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
