package in.myfarm.api.catalog;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Runs once at application startup, after Flyway has migrated/seeded the
// catalog tables and before the app starts accepting traffic -- see
// CatalogSearchIndexService for why this is a full reindex rather than
// something write-path-triggered.
@Component
class CatalogSearchIndexInitializer implements ApplicationRunner {

	private final CatalogSearchIndexService indexService;

	CatalogSearchIndexInitializer(CatalogSearchIndexService indexService) {
		this.indexService = indexService;
	}

	@Override
	public void run(ApplicationArguments args) {
		indexService.reindexAll();
	}
}
