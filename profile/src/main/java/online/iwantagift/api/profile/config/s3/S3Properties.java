package online.iwantagift.api.profile.config.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("iwag.s3")
public record S3Properties(
        String endpoint,
        String region,
        String bucket,
        String accessKey,
        String secretKey
) {
}
