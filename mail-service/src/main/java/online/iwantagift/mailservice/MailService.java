package online.iwantagift.mailservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;

    // Most SMTP providers (beget included) require the From to match the authenticated mailbox.
    @Value("${spring.mail.username}")
    private String from;

    public void send(SimpleMailMessage message){
        if (message.getFrom() == null) {
            message.setFrom(from);
        }
        String[] recipients = message.getTo();
        log.info("Sending mail '{}' to {}", message.getSubject(), Arrays.toString(recipients));
        try {
            mailSender.send(message);
            log.info("Mail '{}' sent to {}", message.getSubject(), Arrays.toString(recipients));
        } catch (MailException ex) {
            log.error("Failed to send mail '{}' to {}", message.getSubject(),
                    Arrays.toString(recipients), ex);
            throw ex;
        }
    }
}
