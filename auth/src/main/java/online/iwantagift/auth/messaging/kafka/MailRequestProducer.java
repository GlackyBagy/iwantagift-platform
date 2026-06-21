package online.iwantagift.auth.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.auth.models.events.CredentialsUpdateEvent;
import online.iwantagift.auth.models.events.DataDeleteEvent;
import online.iwantagift.auth.models.events.PasswordResetEvent;
import online.iwantagift.auth.models.events.MailRequestEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailRequestProducer {

    public static final String PASSWORD_RESET_TOPIC = "passwordReset";
    public static final String PASSWORD_RESET_REQUEST_TOPIC = "passwordResetRequest";
    public static final String EMAIL_CHANGE_TOPIC = "emailChange";
    public static final String EMAIL_VERIFY_TOPIC = "emailVerify";
    public static final String ACCOUNT_DELETE_CONFIRMATION_TOPIC = "deleteAccount";
    public static final String DATA_DELETE_CONFIRMATION_TOPIC = "deleteAllConfirmation";
    public static final String DATA_DELETE_TOPIC = "deleteAll";

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

    public void sendPasswordResetRequest(MailRequestEvent event){
        publish(PASSWORD_RESET_REQUEST_TOPIC, event.email(), asString(event));
    }

    public void sendAccountDeleteConfirmation(MailRequestEvent event){
        publish(ACCOUNT_DELETE_CONFIRMATION_TOPIC, event.email(), asString(event));
    }

    public void sendDataDeleteConfirmation(MailRequestEvent event){
        publish(DATA_DELETE_CONFIRMATION_TOPIC, event.email(), asString(event));
    }

    public void sendDataDeleteRequest(DataDeleteEvent event){
        kafkaTemplate.send(DATA_DELETE_TOPIC, asString(event));
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
