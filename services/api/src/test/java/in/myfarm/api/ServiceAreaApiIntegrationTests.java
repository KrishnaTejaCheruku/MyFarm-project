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
@Sql("/service-area-test-data.sql")
class ServiceAreaApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listsOnlyActivePremiumServiceAreas() throws Exception {
		mockMvc.perform(get("/api/v1/service-areas"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].code").value("seethammadhara"))
				.andExpect(jsonPath("$[0].name.en").value("Seethammadhara"))
				.andExpect(jsonPath("$[0].name.te").value("సీతమ్మధార"))
				.andExpect(jsonPath("$[0].city").value("Visakhapatnam"))
				.andExpect(jsonPath("$[0].timezone").value("Asia/Kolkata"))
				.andExpect(jsonPath("$[0].subscriptionRequired").value(true));
	}

	@Test
	void acceptsAnActiveAreaAndPincodeCombination() throws Exception {
		mockMvc.perform(get("/api/v1/service-areas/eligibility")
						.param("area", "seethammadhara")
						.param("pincode", "530013"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requestedAreaCode")
						.value("seethammadhara"))
				.andExpect(jsonPath("$.pincode").value("530013"))
				.andExpect(jsonPath("$.serviceable").value(true))
				.andExpect(jsonPath("$.serviceArea.subscriptionRequired")
						.value(true));
	}

	@Test
	void rejectsAnUnmappedPincodeAsANormalBusinessResult() throws Exception {
		mockMvc.perform(get("/api/v1/service-areas/eligibility")
						.param("area", "seethammadhara")
						.param("pincode", "530001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.serviceable").value(false))
				.andExpect(jsonPath("$.serviceArea.code")
						.value("seethammadhara"));
	}

	@Test
	void rejectsAnUnknownOrInactiveAreaAsANormalBusinessResult() throws Exception {
		mockMvc.perform(get("/api/v1/service-areas/eligibility")
						.param("area", "inactive-area")
						.param("pincode", "530013"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.serviceable").value(false));
	}

	@Test
	void rejectsMalformedPincodes() throws Exception {
		mockMvc.perform(get("/api/v1/service-areas/eligibility")
						.param("area", "seethammadhara")
						.param("pincode", "53001"))
				.andExpect(status().isBadRequest());
	}
}
