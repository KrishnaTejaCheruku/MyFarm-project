package in.myfarm.worker;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
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
}
