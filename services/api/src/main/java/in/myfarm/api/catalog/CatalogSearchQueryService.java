package in.myfarm.api.catalog;

import java.io.IOException;
import java.util.List;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

// Read side of catalog search -- see CatalogSearchIndexService for how
// the myfarm-products index gets populated.
@Service
class CatalogSearchQueryService {

	private final OpenSearchClient openSearchClient;

	CatalogSearchQueryService(OpenSearchClient openSearchClient) {
		this.openSearchClient = openSearchClient;
	}

	CatalogResponses.Page<CatalogResponses.SearchHit> search(
			String query, int page, int size) {
		try {
			SearchResponse<ProductSearchDocument> response = openSearchClient.search(s -> s
					.index(CatalogSearchIndexService.PRODUCTS_INDEX)
					.from(page * size)
					.size(size)
					.query(q -> q.multiMatch(mm -> mm
							.query(query)
							.fields("nameEn", "nameTe", "descriptionEn", "descriptionTe"))),
					ProductSearchDocument.class);

			List<CatalogResponses.SearchHit> hits = response.hits().hits().stream()
					.map(Hit::source)
					.map(CatalogSearchQueryService::toSearchHit)
					.toList();

			long totalElements = response.hits().total() != null
					? response.hits().total().value()
					: hits.size();
			int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

			return new CatalogResponses.Page<>(
					hits, page, size, totalElements, totalPages);
		} catch (IOException e) {
			throw new CatalogSearchUnavailableException(e);
		}
	}

	private static CatalogResponses.SearchHit toSearchHit(ProductSearchDocument document) {
		return new CatalogResponses.SearchHit(
				document.code(),
				document.slug(),
				document.categoryCode(),
				new CatalogResponses.LocalizedText(
						document.nameEn(), document.nameTe()));
	}
}
