package in.myfarm.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								HttpMethod.GET,
								"/api/v1/catalog/**",
								"/api/v1/service-areas",
								"/api/v1/service-areas/**",
								"/actuator/health",
								"/actuator/health/**",
								"/actuator/info")
						.permitAll()
						.requestMatchers("/actuator/prometheus")
						.hasAuthority("SCOPE_ops:metrics")
						.anyRequest()
						.authenticated());
		return http.build();
	}
}
