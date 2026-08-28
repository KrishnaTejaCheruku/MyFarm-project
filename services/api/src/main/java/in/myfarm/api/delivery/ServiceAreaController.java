package in.myfarm.api.delivery;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/service-areas")
public class ServiceAreaController {

	private static final String AREA_CODE_PATTERN =
			"[a-z0-9]+(?:-[a-z0-9]+)*";
	private static final String PINCODE_PATTERN = "[0-9]{6}";

	private final ServiceAreaQueryService serviceAreaQueryService;
	private final DeliveryOptionQueryService deliveryOptionQueryService;

	ServiceAreaController(
			ServiceAreaQueryService serviceAreaQueryService,
			DeliveryOptionQueryService deliveryOptionQueryService) {
		this.serviceAreaQueryService = serviceAreaQueryService;
		this.deliveryOptionQueryService = deliveryOptionQueryService;
	}

	@GetMapping
	public List<ServiceAreaResponses.ServiceArea> serviceAreas() {
		return serviceAreaQueryService.serviceAreas();
	}

	@GetMapping("/eligibility")
	public ServiceAreaResponses.Eligibility eligibility(
			@RequestParam("area")
			@Size(max = 64)
			@Pattern(regexp = AREA_CODE_PATTERN) String areaCode,
			@RequestParam
			@Pattern(regexp = PINCODE_PATTERN) String pincode) {
		return serviceAreaQueryService.eligibility(areaCode, pincode);
	}

	@GetMapping("/{areaCode}/delivery-options")
	public DeliveryOptionResponses.Options deliveryOptions(
			@PathVariable
			@Size(max = 64)
			@Pattern(regexp = AREA_CODE_PATTERN) String areaCode) {
		return deliveryOptionQueryService.options(areaCode);
	}

	@GetMapping("/{areaCode}/schedule-preview")
	public DeliveryOptionResponses.SchedulePreview schedulePreview(
			@PathVariable
			@Size(max = 64)
			@Pattern(regexp = AREA_CODE_PATTERN) String areaCode,
			@RequestParam("window")
			@Size(max = 64)
			@Pattern(regexp = AREA_CODE_PATTERN) String windowCode,
			@RequestParam("plan")
			@Size(max = 64)
			@Pattern(regexp = AREA_CODE_PATTERN) String planCode,
			@RequestParam LocalDate startsOn) {
		return deliveryOptionQueryService.preview(
				areaCode, windowCode, planCode, startsOn);
	}
}
