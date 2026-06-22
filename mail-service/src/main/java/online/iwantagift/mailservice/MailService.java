package online.iwantagift.mailservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final SesClient sesClient;

    @Value("${postbox.from}")
    private String from;

    public void send(MailMessage message) {
        log.info("Sending mail '{}' to {}", message.subject(), message.to());
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(from)
                    .destination(Destination.builder().toAddresses(message.to()).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(message.subject()).charset("UTF-8").build())
                            .body(Body.builder()
                                    .text(Content.builder().data(message.text()).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build();
            sesClient.sendEmail(request);
            log.info("Mail '{}' sent to {}", message.subject(), message.to());
        } catch (SesException ex) {
            log.error("Failed to send mail '{}' to {}", message.subject(), message.to(), ex);
            throw ex;
        }
    }
}
