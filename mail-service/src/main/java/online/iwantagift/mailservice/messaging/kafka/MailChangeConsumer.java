package online.iwantagift.mailservice.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.mailservice.MailService;
import online.iwantagift.mailservice.MessageBuilder;
import online.iwantagift.mailservice.models.events.CredentialsUpdateEvent;
import online.iwantagift.mailservice.models.events.PasswordResetEvent;
import online.iwantagift.mailservice.models.events.PasswordResetRequestEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailChangeConsumer {

    public static final String PASSWORD_RESET_TOPIC = "passwordReset";
    public static final String PASSWORD_RESET_REQUEST_TOPIC = "passwordResetRequest";
    public static final String EMAIL_CHANGE_TOPIC = "emailChange";
    public static final String EMAIL_VERIFY_TOPIC = "emailVerify";

    private final ObjectMapper objectMapper;
    private final MailService mailService;

    @KafkaListener(topics = EMAIL_CHANGE_TOPIC)
    void handleEmailChange(String eventStr) {
        log.info("Received emailChange event from Kafka");
        CredentialsUpdateEvent event = objectMapper.readValue(eventStr, CredentialsUpdateEvent.class);
        log.info("Processing email change: sending verification mail to {}", event.newEmail());
        mailService.send(
                MessageBuilder.buildForUpdateEmail(event.newEmail(), event.verificationUrl())
        );
        log.info("Sent mail change message to {}", event.newEmail());
    }

    @KafkaListener(topics = EMAIL_VERIFY_TOPIC)
    void handleEmailVerify(String eventStr){
        CredentialsUpdateEvent event = objectMapper.readValue(eventStr, CredentialsUpdateEvent.class);
        mailService.send(
                MessageBuilder.buildForEmailVerification(event.oldEmail(), event.verificationUrl())
        );
    }

    @KafkaListener(topics = PASSWORD_RESET_REQUEST_TOPIC)
    void handlePasswordResetRequest(String eventStr){
        PasswordResetRequestEvent event = objectMapper.readValue(eventStr, PasswordResetRequestEvent.class);
        mailService.send(
                MessageBuilder.buildForPasswordResetRequest(event.email(), event.confirmUrl())
        );
    }

    @KafkaListener(topics = PASSWORD_RESET_TOPIC)
    void handlePasswordReset(String eventStr){
        PasswordResetEvent event = objectMapper.readValue(eventStr, PasswordResetEvent.class);
        mailService.send(
                MessageBuilder.buildForPasswordReset(event.email(), event.newPassword())
        );
    }
}
