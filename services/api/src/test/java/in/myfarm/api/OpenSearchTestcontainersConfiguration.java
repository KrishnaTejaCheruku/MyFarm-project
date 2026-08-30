package in.myfarm.api;

import org.opensearch.testcontainers.OpenSearchContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.utility.DockerImageName;

/**
 * Deliberately NOT part of the shared TestcontainersConfiguration.
 *
 * OpenSearch's own docs state a minimum of 4GB of Docker RAM for
 * OpenSearch alone (docs.opensearch.org install/docker). On a dev
 * machine where Docker Desktop is itself capped at 4GB total, running
 * this container in the same shared context as MariaDB, RabbitMQ,
 * Valkey and Keycloak (all 5 up at once, for every single test in the
 * suite) blows the memory budget -- confirmed by reproducible
 * Keycloak container health-check failures across the WHOLE suite,
 * not just the search tests, once OpenSearch joined the shared config.
 *
 * Keeping this in its own @TestConfiguration, imported only by the
 * test class that actually needs search, means the other ~30 tests
 * never pay OpenSearch's memory cost. Paired with a dedicated Surefire
 * execution (see pom.xml) that forks a fresh JVM for
 * CatalogSearchApiIntegrationTests, the other containers are torn down
 * (JVM exit runs Testcontainers' shutdown hooks) before this one
 * starts, so OpenSearch effectively gets the whole 4GB to itself.
 *
 * OPENSEARCH_JAVA_OPTS and bootstrap.memory_lock=false are set to keep
 * OpenSearch's own footprint as small as possible and to avoid a
 * documented bug (opensearch-project/OpenSearch#5865) where
 * bootstrap.memory_lock=true forces an attempt to lock ~1GB natively
 * regardless of a lower configured -Xmx.
 */
@TestConfiguration(proxyBeanMethods = false)
class OpenSearchTestcontainersConfiguration {

	@Bean
	OpenSearchContainer<?> openSearchContainer() {
		return new OpenSearchContainer<>(
				DockerImageName.parse("opensearchproject/opensearch:3.1.0"))
				.withEnv("OPENSEARCH_JAVA_OPTS", "-Xms512m -Xmx512m")
				.withEnv("bootstrap.memory_lock", "false");
	}

	// getHttpHostAddress() already returns a full "http://host:port"
	// string (confirmed against opensearch-testcontainers' own source:
	// it builds "(scheme) + getHost() + \":\" + mappedPort" itself) --
	// prepending "http://" here a second time produced a malformed
	// double-scheme URL ("http://http://host:port"), which
	// java.net.URI parsed by treating the literal string "http" as the
	// hostname, causing every OpenSearch call to fail with
	// UnknownHostException. Passing the value straight through fixes
	// it.
	@Bean
	DynamicPropertyRegistrar openSearchPropertiesRegistrar(
			OpenSearchContainer<?> openSearchContainer) {
		return registry -> registry.add("myfarm.opensearch.base-url",
				openSearchContainer::getHttpHostAddress);
	}
}
