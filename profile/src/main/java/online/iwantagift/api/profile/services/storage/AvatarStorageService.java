package online.iwantagift.api.profile.services.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.api.profile.config.s3.S3Properties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarStorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public String upload(UUID profileId, UUID avatarId, String contentType, long sizeBytes, InputStream content) {
        String storageKey = storageKey(profileId, avatarId);

        log.info("S3 Upload Config: endpoint={}, bucket={}, region={}, key={}",
                s3Properties.endpoint(),
                s3Properties.bucket(),
                s3Properties.region(),
                storageKey);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(storageKey)
                .contentType(contentType)
                .contentLength(sizeBytes)
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromInputStream(content, sizeBytes));
        } catch (S3Exception e) {
            log.warn(
                    "Avatar upload failed in S3: status={}, errorCode={}, message={}, requestId={}, bucket={}, key={}, sizeBytes={}, contentType={}",
                    e.statusCode(),
                    e.awsErrorDetails() == null ? null : e.awsErrorDetails().errorCode(),
                    e.awsErrorDetails() == null ? null : e.awsErrorDetails().errorMessage(),
                    e.requestId(),
                    s3Properties.bucket(),
                    storageKey,
                    sizeBytes,
                    contentType
            );
            throw e;
        }

        return storageKey;
    }

    public void delete(String storageKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(storageKey)
                .build();

        s3Client.deleteObject(request);
    }

    public String publicUrl(String storageKey) {
        return "%s/%s/%s".formatted(
                s3Properties.endpoint().replaceAll("/+$", ""),
                s3Properties.bucket(),
                storageKey
        );
    }

    private String storageKey(UUID profileId, UUID avatarId) {
        return "avatars/users/%s/%s".formatted(profileId, avatarId);
    }
}
