package in.myfarm.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Deliberately does NOT override catalog data with @Sql -- this exercises
 * the real V4__seed_catalog_data.sql products, the same data
 * CatalogSearchIndexInitializer indexes at startup for actual local dev.
 * See CatalogSearchIndexService for why indexing happens once at startup
 * rather than per-write (there's no catalog write path yet).
 */
@Import({ TestcontainersConfiguration.class, OpenSearchTestcontainersConfiguration.class })
@SpringBootTest
@AutoConfigureMockMvc
class CatalogSearchApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void searchingByEnglishNameReturnsTheMatchingProduct() throws Exception {
		mockMvc.perform(get("/api/v1/catalog/search").param("q", "mango"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.items[0].code").value("mango"))
				.andExpect(jsonPath("$.items[0].slug").value("mango"))
				.andExpect(jsonPath("$.items[0].categoryCode").value("fruits"))
				.andExpect(jsonPath("$.items[0].name.en")
						.value("Mango (Banganapalli)"));
	}

	@Test
	void searchingByTeluguNameReturnsTheMatchingProduct() throws Exception {
		mockMvc.perform(get("/api/v1/catalog/search").param("q", "గోంగూర"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].code").value("gongura"));
	}

	@Test
	void searchingByAnEnglishDescriptionWordMatchesAcrossFields() throws Exception {
		// "Andhra" only appears in gongura's description_en, not its name --
		// proves the search actually queries descriptionEn/descriptionTe
		// too, not just the name fields.
		mockMvc.perform(get("/api/v1/catalog/search").param("q", "Andhra"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].code").value("gongura"));
	}

	@Test
	void searchWithNoMatchesReturnsAnEmptyPage() throws Exception {
		mockMvc.perform(get("/api/v1/catalog/search").param("q", "xyznonexistentterm"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(0))
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void blankQueryIsRejected() throws Exception {
		mockMvc.perform(get("/api/v1/catalog/search").param("q", "  "))
				.andExpect(status().isBadRequest());
	}
}
