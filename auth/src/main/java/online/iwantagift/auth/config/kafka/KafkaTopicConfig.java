package online.iwantagift.auth.config.kafka;

import online.iwantagift.auth.config.IwagProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@Profile("dev")
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics kafkaTopics(IwagProperties iwagProperties) {
        NewTopic[] topics = iwagProperties.getUsingKafkaTopics()
                .stream()
                .distinct()
                .map(topic -> TopicBuilder.name(topic).build())
                .toArray(NewTopic[]::new);

        return new KafkaAdmin.NewTopics(topics);
    }
}
