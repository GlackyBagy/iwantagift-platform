package online.iwantagift.auth.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.auth.models.events.CredentialsUpdateEvent;
import online.iwantagift.auth.models.events.PasswordResetEvent;
import online.iwantagift.auth.models.events.PasswordResetRequestEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class CredentialsProducer {

    public static final String PASSWORD_RESET_TOPIC = "passwordReset";
    public static final String PASSWORD_RESET_REQUEST_TOPIC = "passwordResetRequest";
    public static final String EMAIL_CHANGE_TOPIC = "emailChange";
    public static final String EMAIL_VERIFY_TOPIC = "emailVerify";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendEmailChange(CredentialsUpdateEvent event) {
        publish(EMAIL_CHANGE_TOPIC, event.oldEmail(), asString(event));
    }

    public void sendEmailVerify(CredentialsUpdateEvent event){
        publish(EMAIL_VERIFY_TOPIC, event.oldEmail(), asString(event));
    }

    public void sendPasswordReset(PasswordResetEvent event){
        publish(PASSWORD_RESET_TOPIC, event.email(), asString(event));
    }

    public void sendPasswordResetRequest(PasswordResetRequestEvent event){
        publish(PASSWORD_RESET_REQUEST_TOPIC, event.email(), asString(event));
    }

    private void publish(String topic, String key, String payload) {
        log.info("Publishing event to Kafka topic '{}' (key={})", topic, key);
        kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to Kafka topic '{}' (key={})", topic, key, ex);
            } else {
                log.info("Published event to Kafka topic '{}' (key={}, partition={}, offset={})",
                        topic, key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private String asString(Object event){
        return objectMapper.writeValueAsString(event);
    }
}
