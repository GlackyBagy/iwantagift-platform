package online.iwantagift.mailservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

import java.net.URI;

@Configuration
public class SesConfig {

    @Value("${postbox.endpoint}")
    private String endpoint;

    @Value("${postbox.region}")
    private String region;

    @Value("${postbox.access-key}")
    private String accessKey;

    @Value("${postbox.secret-key}")
    private String secretKey;

    @Bean
    public SesV2Client sesClient() {
        return SesV2Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
