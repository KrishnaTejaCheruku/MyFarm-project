package in.myfarm.api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

	@Value("${myfarm.cors.allowed-origins}")
	private List<String> allowedOrigins;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
						// Pre-auth by definition -- this is how a customer
						// gets a token in the first place.
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/otp/**")
						.permitAll()
						// Plays the role a real gateway's unauthenticated,
						// signature-verified webhook would play (see the
						// payment package's MockPaymentSimulatorController)
						// -- no user session involved, same as the OTP
						// endpoints above.
						.requestMatchers(HttpMethod.POST, "/api/v1/payments/mock/**")
						.permitAll()
						// Identity phase landed: guest checkout is over,
						// placing an order now requires a customer-role
						// token minted via the OTP flow above.
						.requestMatchers(HttpMethod.POST, "/api/v1/orders")
						.hasRole("customer")
						.requestMatchers(HttpMethod.GET, "/api/v1/orders/**")
						.permitAll()
						.requestMatchers("/api/v1/admin/**")
						.hasRole("admin")
						.anyRequest()
						.authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(
								jwtAuthenticationConverter())));
		return http.build();
	}

	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(
				new KeycloakJwtAuthoritiesConverter());
		return converter;
	}

	private CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(allowedOrigins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
		configuration.setAllowedHeaders(List.of("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
