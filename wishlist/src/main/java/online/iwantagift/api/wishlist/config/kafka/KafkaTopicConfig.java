package online.iwantagift.api.wishlist.config.kafka;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.config.IwagProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {
    private final IwagProperties iwagProperties;

    @Bean
    public KafkaAdmin.NewTopics newEntityTopics() {
        NewTopic[] topics = iwagProperties.getRequiredService("price")
                .getKafkaTopics()
                .stream()
                .map(topicName -> TopicBuilder.name(topicName).build())
                .toArray(NewTopic[]::new);

        return new KafkaAdmin.NewTopics(topics);
    }
}
