package in.myfarm.api.delivery;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

public final class DeliveryOptionResponses {

	private DeliveryOptionResponses() {
	}

	public record LocalizedText(String en, String te) {
	}

	public record Window(
			String code,
			LocalizedText name,
			LocalTime startsAt,
			LocalTime endsAt,
			int cutoffMinutesBefore) {
	}

	public record Plan(
			String code,
			LocalizedText name,
			String billingPeriod,
			int durationMonths,
			String deliveryFrequency) {
	}

	public record Options(
			String serviceAreaCode,
			String timezone,
			List<Window> windows,
			List<Plan> plans) {

		public Options {
			windows = List.copyOf(windows);
			plans = List.copyOf(plans);
		}
	}

	public record SchedulePreview(
			String serviceAreaCode,
			String timezone,
			String windowCode,
			String planCode,
			LocalDate startsOn,
			LocalDate endsOn,
			long deliveryCount,
			boolean operatesEveryCalendarDay,
			ZonedDateTime firstOrderCutoff) {
	}
}
