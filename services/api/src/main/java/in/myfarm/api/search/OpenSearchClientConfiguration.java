package in.myfarm.api.search;

import java.net.URI;

import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// No official Spring Boot starter for OpenSearch, and Spring Data
// OpenSearch's latest release doesn't claim support for this Boot/
// Framework version yet (see pom.xml comment) -- this wires the plain
// OpenSearch Java client directly instead. Security is disabled on
// every OpenSearch instance this app talks to today (local dev
// compose, Testcontainers) so there's no auth to configure here; a
// real deployment will need that added when it exists.
@Configuration(proxyBeanMethods = false)
public class OpenSearchClientConfiguration {

	@Bean
	public OpenSearchClient openSearchClient(
			@Value("${myfarm.opensearch.base-url}") String baseUrl) {
		URI uri = URI.create(baseUrl);
		HttpHost host = new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort());
		OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
				.builder(host)
				.build();
		return new OpenSearchClient(transport);
	}
}
