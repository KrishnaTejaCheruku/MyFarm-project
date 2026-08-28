package in.myfarm.api.delivery;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DeliveryOptionQueryService {

	private final ServiceAreaRepository serviceAreaRepository;
	private final DeliveryWindowRepository windowRepository;
	private final SubscriptionPlanRepository planRepository;

	DeliveryOptionQueryService(
			ServiceAreaRepository serviceAreaRepository,
			DeliveryWindowRepository windowRepository,
			SubscriptionPlanRepository planRepository) {
		this.serviceAreaRepository = serviceAreaRepository;
		this.windowRepository = windowRepository;
		this.planRepository = planRepository;
	}

	DeliveryOptionResponses.Options options(String areaCode) {
		ServiceAreaEntity area = activeArea(areaCode);
		List<DeliveryOptionResponses.Window> windows = windowRepository
				.findByServiceArea_IdAndActiveTrueOrderBySortOrderAscIdAsc(area.id())
				.stream()
				.map(this::toWindow)
				.toList();
		List<DeliveryOptionResponses.Plan> plans = planRepository
				.findByServiceArea_IdAndActiveTrueOrderBySortOrderAscIdAsc(area.id())
				.stream()
				.map(this::toPlan)
				.toList();
		return new DeliveryOptionResponses.Options(
				area.code(), area.timezone(), windows, plans);
	}

	DeliveryOptionResponses.SchedulePreview preview(
			String areaCode,
			String windowCode,
			String planCode,
			LocalDate startsOn) {
		ServiceAreaEntity area = activeArea(areaCode);
		DeliveryWindowEntity window = windowRepository
				.findByServiceArea_IdAndCodeAndActiveTrue(area.id(), windowCode)
				.orElseThrow(() -> new DeliveryOptionNotFoundException(
						"delivery window", windowCode));
		SubscriptionPlanEntity plan = planRepository
				.findByServiceArea_IdAndCodeAndActiveTrue(area.id(), planCode)
				.orElseThrow(() -> new DeliveryOptionNotFoundException(
						"subscription plan", planCode));

		LocalDate endsExclusive = startsOn.plusMonths(plan.durationMonths());
		LocalDate endsOn = endsExclusive.minusDays(1);
		long deliveryCount = ChronoUnit.DAYS.between(startsOn, endsExclusive);
		ZonedDateTime firstOrderCutoff = startsOn
				.atTime(window.startsAt())
				.atZone(ZoneId.of(area.timezone()))
				.minusMinutes(window.cutoffMinutesBefore());

		return new DeliveryOptionResponses.SchedulePreview(
				area.code(),
				area.timezone(),
				window.code(),
				plan.code(),
				startsOn,
				endsOn,
				deliveryCount,
				true,
				firstOrderCutoff);
	}

	private ServiceAreaEntity activeArea(String areaCode) {
		return serviceAreaRepository.findByCodeAndActiveTrue(areaCode)
				.orElseThrow(() -> new DeliveryOptionNotFoundException(
						"service area", areaCode));
	}

	private DeliveryOptionResponses.Window toWindow(
			DeliveryWindowEntity window) {
		return new DeliveryOptionResponses.Window(
				window.code(),
				new DeliveryOptionResponses.LocalizedText(
						window.nameEn(), window.nameTe()),
				window.startsAt(),
				window.endsAt(),
				window.cutoffMinutesBefore());
	}

	private DeliveryOptionResponses.Plan toPlan(
			SubscriptionPlanEntity plan) {
		return new DeliveryOptionResponses.Plan(
				plan.code(),
				new DeliveryOptionResponses.LocalizedText(
						plan.nameEn(), plan.nameTe()),
				plan.billingPeriod().name(),
				plan.durationMonths(),
				"DAILY");
	}
}
