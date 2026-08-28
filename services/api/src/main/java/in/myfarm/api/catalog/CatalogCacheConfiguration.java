package in.myfarm.api.catalog;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

// Backs @Cacheable in CatalogQueryService via Valkey (spring.cache.type=redis
// in application.properties -- Valkey speaks the Redis protocol, so Spring
// Data Redis's autoconfiguration works against it unchanged). Local
// dev/tests get Valkey from TestcontainersConfiguration's @ServiceConnection
// container; a persistent deployment needs its own Valkey instance wired
// the same way infra/identity/docker-compose.yml wires Keycloak.
@Configuration(proxyBeanMethods = false)
@EnableCaching
class CatalogCacheConfiguration {
}
