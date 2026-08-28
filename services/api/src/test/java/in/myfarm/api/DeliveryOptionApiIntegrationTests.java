package in.myfarm.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Sql("/delivery-option-test-data.sql")
class DeliveryOptionApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listsOnlyActiveWindowsAndPlansForTheArea() throws Exception {
		mockMvc.perform(get(
				"/api/v1/service-areas/seethammadhara/delivery-options"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.serviceAreaCode")
						.value("seethammadhara"))
				.andExpect(jsonPath("$.timezone").value("Asia/Kolkata"))
				.andExpect(jsonPath("$.windows.length()").value(1))
				.andExpect(jsonPath("$.windows[0].code").value("morning"))
				.andExpect(jsonPath("$.windows[0].cutoffMinutesBefore")
						.value(480))
				.andExpect(jsonPath("$.plans.length()").value(2))
				.andExpect(jsonPath("$.plans[0].billingPeriod")
						.value("MONTHLY"))
				.andExpect(jsonPath("$.plans[1].billingPeriod")
						.value("YEARLY"))
				.andExpect(jsonPath("$.plans[1].deliveryFrequency")
						.value("DAILY"));
	}

	@Test
	void previewsEveryCalendarDayInAMonthlyPlan() throws Exception {
		mockMvc.perform(get(
				"/api/v1/service-areas/seethammadhara/schedule-preview")
						.param("window", "morning")
						.param("plan", "monthly")
						.param("startsOn", "2027-01-01"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.startsOn").value("2027-01-01"))
				.andExpect(jsonPath("$.endsOn").value("2027-01-31"))
				.andExpect(jsonPath("$.deliveryCount").value(31))
				.andExpect(jsonPath("$.operatesEveryCalendarDay")
						.value(true))
				.andExpect(jsonPath("$.firstOrderCutoff")
						.value("2026-12-31T21:30:00+05:30"));
	}

	@Test
	void previewsAll365DaysInANonLeapYear() throws Exception {
		mockMvc.perform(get(
				"/api/v1/service-areas/seethammadhara/schedule-preview")
						.param("window", "morning")
						.param("plan", "yearly")
						.param("startsOn", "2027-01-01"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.endsOn").value("2027-12-31"))
				.andExpect(jsonPath("$.deliveryCount").value(365))
				.andExpect(jsonPath("$.operatesEveryCalendarDay")
						.value(true));
	}

	@Test
	void accountsForLeapDayInsteadOfHardCoding365() throws Exception {
		mockMvc.perform(get(
				"/api/v1/service-areas/seethammadhara/schedule-preview")
						.param("window", "morning")
						.param("plan", "yearly")
						.param("startsOn", "2028-01-01"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.endsOn").value("2028-12-31"))
				.andExpect(jsonPath("$.deliveryCount").value(366));
	}

	@Test
	void rejectsUnknownOrInactiveOptions() throws Exception {
		mockMvc.perform(get(
				"/api/v1/service-areas/seethammadhara/schedule-preview")
						.param("window", "inactive-window")
						.param("plan", "monthly")
						.param("startsOn", "2027-01-01"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.type")
						.value("urn:myfarm:problem:delivery-option-not-found"))
				.andExpect(jsonPath("$.option").value("delivery window"));
	}

	@Test
	void rejectsMalformedStartDates() throws Exception {
		mockMvc.perform(get(
				"/api/v1/service-areas/seethammadhara/schedule-preview")
						.param("window", "morning")
						.param("plan", "monthly")
						.param("startsOn", "01-01-2027"))
				.andExpect(status().isBadRequest());
	}
}
