package online.iwantagift.mailservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import static online.iwantagift.mailservice.messaging.kafka.MailChangeConsumer.EMAIL_CHANGE_TOPIC;
import static online.iwantagift.mailservice.messaging.kafka.MailChangeConsumer.EMAIL_VERIFY_TOPIC;
import static online.iwantagift.mailservice.messaging.kafka.MailChangeConsumer.PASSWORD_RESET_TOPIC;
import static online.iwantagift.mailservice.messaging.kafka.MailChangeConsumer.PASSWORD_RESET_REQUEST_TOPIC;

@Configuration
@Profile("dev")
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics mailTopics() {
        NewTopic emailChangeTopic = TopicBuilder.name(EMAIL_CHANGE_TOPIC).build();
        NewTopic emailVerifyTopic = TopicBuilder.name(EMAIL_VERIFY_TOPIC).build();
        NewTopic passwordResetTopic = TopicBuilder.name(PASSWORD_RESET_TOPIC).build();
        NewTopic passwordResetRequestTopic = TopicBuilder.name(PASSWORD_RESET_REQUEST_TOPIC).build();
        return new KafkaAdmin.NewTopics(
                emailChangeTopic, emailVerifyTopic, passwordResetTopic, passwordResetRequestTopic);
    }
}
