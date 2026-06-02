package online.iwantagift.api.profile;

import online.iwantagift.api.profile.config.IwagProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IwagProperties.class)
public class ProfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileApplication.class, args);
    }

}
