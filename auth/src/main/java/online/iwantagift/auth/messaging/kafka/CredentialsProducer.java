package online.iwantagift.auth.messaging.kafka;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.models.events.CredentialsUpdateEvent;
import online.iwantagift.auth.models.events.PasswordResetEvent;
import online.iwantagift.auth.models.events.PasswordResetRequestEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CredentialsProducer {

    public static final String PASSWORD_RESET_TOPIC = "passwordReset";
    public static final String PASSWORD_RESET_REQUEST_TOPIC = "passwordResetRequest";
    public static final String EMAIL_CHANGE_TOPIC = "emailChange";
    public static final String EMAIL_VERIFY_TOPIC = "emailVerify";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendEmailChange(CredentialsUpdateEvent event) {
        kafkaTemplate.send(EMAIL_CHANGE_TOPIC, event.oldEmail(), asString(event));
    }

    public void sendEmailVerify(CredentialsUpdateEvent event){
        kafkaTemplate.send(EMAIL_VERIFY_TOPIC, event.oldEmail(), asString(event));
    }

    public void sendPasswordReset(PasswordResetEvent event){
        kafkaTemplate.send(PASSWORD_RESET_TOPIC, event.email(), asString(event));
    }

    public void sendPasswordResetRequest(PasswordResetRequestEvent event){
        kafkaTemplate.send(PASSWORD_RESET_REQUEST_TOPIC, event.email(), asString(event));
    }

    private String asString(Object event){
        return objectMapper.writeValueAsString(event);
    }
}
