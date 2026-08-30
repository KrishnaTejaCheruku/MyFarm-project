package in.myfarm.api.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Indexing side of catalog search (see CatalogSearchQueryService for the
// read side). There's no catalog write path yet -- categories/products
// only change via Flyway seed data or direct DB access -- so this is a
// full reindex triggered once at startup (CatalogSearchIndexInitializer)
// rather than incremental updates hung off a write path that doesn't
// exist. Revisit once an admin catalog write endpoint exists: that
// endpoint should call reindexAll() (or a smarter per-product upsert)
// after each change instead of relying on the next restart.
@Service
class CatalogSearchIndexService {

	static final String PRODUCTS_INDEX = "myfarm-products";

	private static final Logger log = LoggerFactory.getLogger(CatalogSearchIndexService.class);

	private final OpenSearchClient openSearchClient;
	private final ProductRepository productRepository;

	CatalogSearchIndexService(
			OpenSearchClient openSearchClient, ProductRepository productRepository) {
		this.openSearchClient = openSearchClient;
		this.productRepository = productRepository;
	}

	@Transactional(readOnly = true)
	void reindexAll() {
		try {
			ensureIndexExists();
			List<ProductEntity> products = productRepository.findByActiveTrue();
			if (products.isEmpty()) {
				log.info("Catalog search reindex: no active products to index.");
				return;
			}

			List<BulkOperation> operations = new ArrayList<>();
			for (ProductEntity product : products) {
				IndexOperation<ProductSearchDocument> indexOperation =
						new IndexOperation.Builder<ProductSearchDocument>()
								.index(PRODUCTS_INDEX)
								.id(product.code())
								.document(toDocument(product))
								.build();
				operations.add(new BulkOperation.Builder().index(indexOperation).build());
			}

			BulkRequest bulkRequest = new BulkRequest.Builder()
					.index(PRODUCTS_INDEX)
					.operations(operations)
					.refresh(Refresh.True)
					.build();
			BulkResponse response = openSearchClient.bulk(bulkRequest);
			if (response.errors()) {
				log.warn("Catalog search reindex completed with per-item errors: {}",
						response.items());
			} else {
				log.info("Catalog search reindex: indexed {} active products.",
						products.size());
			}
		} catch (IOException e) {
			// Startup indexing failing shouldn't take the whole app down --
			// search just won't return results (or will 503, see
			// CatalogSearchQueryService/CatalogExceptionHandler) until
			// OpenSearch is reachable and this runs again on next restart.
			log.warn("Catalog search reindex failed -- search will be "
					+ "unavailable until this succeeds.", e);
		}
	}

	private void ensureIndexExists() throws IOException {
		boolean exists = openSearchClient.indices()
				.exists(e -> e.index(PRODUCTS_INDEX))
				.value();
		if (exists) {
			return;
		}
		openSearchClient.indices().create(c -> c
				.index(PRODUCTS_INDEX)
				.mappings(m -> m
						.properties("code", p -> p.keyword(k -> k))
						.properties("slug", p -> p.keyword(k -> k))
						.properties("categoryCode", p -> p.keyword(k -> k))
						.properties("nameEn", p -> p.text(t -> t))
						.properties("nameTe", p -> p.text(t -> t))
						.properties("descriptionEn", p -> p.text(t -> t))
						.properties("descriptionTe", p -> p.text(t -> t))));
	}

	private static ProductSearchDocument toDocument(ProductEntity product) {
		return new ProductSearchDocument(
				product.code(),
				product.slug(),
				product.category().code(),
				product.nameEn(),
				product.nameTe(),
				product.descriptionEn(),
				product.descriptionTe());
	}
}
