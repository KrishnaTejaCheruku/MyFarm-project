package in.myfarm.api.identity;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class IdentityConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
