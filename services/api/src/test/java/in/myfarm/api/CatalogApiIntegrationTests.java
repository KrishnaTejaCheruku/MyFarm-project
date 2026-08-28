package in.myfarm.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Sql("/catalog-test-data.sql")
class CatalogApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsOnlyActiveCategoriesInBusinessOrder() throws Exception {
		mockMvc.perform(get("/api/v1/catalog/categories"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].code").value("milk"))
				.andExpect(jsonPath("$[0].name.en").value("Milk"))
				.andExpect(jsonPath("$[0].name.te").value("పాలు"))
				.andExpect(jsonPath("$[1].code").value("eggs"));
	}

	@Test
	void filtersProductsAndReturnsOnlyActiveVariants() throws Exception {
		mockMvc.perform(get("/api/v1/catalog/products")
						.param("category", "milk")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].slug").value("cow-milk"))
				.andExpect(jsonPath("$.items[0].variants.length()").value(2))
				.andExpect(jsonPath("$.items[0].variants[0].sku")
						.value("MILK-COW-500ML"))
				.andExpect(jsonPath("$.items[0].variants[0].unit")
						.value("millilitre"))
				.andExpect(jsonPath("$.items[0].variants[0].price.currency")
						.value("INR"))
				.andExpect(jsonPath("$.items[0].variants[0].price.amountInr")
						.value(38))
				.andExpect(jsonPath("$.items[0].variants[0].subscriptionAllowed")
						.value(true));
	}

	@Test
	void returnsOneProductBySlug() throws Exception {
		mockMvc.perform(get("/api/v1/catalog/products/cow-milk"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("cow-milk"))
				.andExpect(jsonPath("$.categoryCode").value("milk"))
				.andExpect(jsonPath("$.name.te").value("ఆవు పాలు"))
				.andExpect(jsonPath("$.variants.length()").value(2));
	}

	@Test
	void returnsProblemDetailForMissingOrInactiveProduct() throws Exception {
		mockMvc.perform(get("/api/v1/catalog/products/inactive-milk"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(
						MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.type").value(
						"urn:myfarm:problem:catalog-product-not-found"))
				.andExpect(jsonPath("$.title").value(
						"Catalogue product not found"))
				.andExpect(jsonPath("$.slug").value("inactive-milk"));
	}
}
