package in.myfarm.api;

import dasniko.testcontainers.keycloak.KeycloakContainer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	MariaDBContainer mariaDbContainer() {
		return new MariaDBContainer(
				DockerImageName.parse("mariadb:11.4.13"));
	}

	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitContainer() {
		return new RabbitMQContainer(
				DockerImageName.parse("rabbitmq:4.3.5-management-alpine"));
	}

	@Bean
	@ServiceConnection(name = "redis")
	GenericContainer<?> valkeyContainer() {
		return new GenericContainer<>(
				DockerImageName.parse("valkey/valkey:8.1.9-alpine"))
				.withExposedPorts(6379);
	}

	// Imports the exact same realm file local dev uses
	// (infra/identity/myfarm-realm.json, symlinked to
	// src/test/resources/myfarm-realm.json) so tests exercise real
	// realm config instead of a hand-mocked one.
	@Bean
	KeycloakContainer keycloakContainer() {
		return new KeycloakContainer("quay.io/keycloak/keycloak:26.7.2")
				.withRealmImportFile("/myfarm-realm.json");
	}

	// No @ServiceConnection here -- Spring Boot doesn't ship a
	// well-known connection-details type for an OAuth2 resource
	// server's issuer-uri. DynamicPropertyRegistrar is the documented
	// way to contribute dynamic properties from inside a
	// @TestConfiguration bean instead of a static @DynamicPropertySource
	// method on every test class that needs one.
	@Bean
	DynamicPropertyRegistrar keycloakPropertiesRegistrar(
			KeycloakContainer keycloakContainer) {
		return registry -> {
			registry.add("myfarm.keycloak.base-url",
					keycloakContainer::getAuthServerUrl);
			registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
					() -> keycloakContainer.getAuthServerUrl() + "/realms/myfarm");
		};
	}
}
