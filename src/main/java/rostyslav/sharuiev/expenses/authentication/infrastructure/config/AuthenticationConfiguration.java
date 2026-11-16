package rostyslav.sharuiev.expenses.authentication.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(AuthenticationProperties.class)
public class AuthenticationConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
