package online.iwantagift.api.profile;

import online.iwantagift.api.profile.services.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
        args = {
                "--iwag.s3.access-key=test-access-key",
                "--iwag.s3.secret-key=test-secret-key"
        })
class ProfileApplicationTests {

    @MockitoBean
    private ProfileService profileService;

    @Test
    void contextLoads() {
    }

}
