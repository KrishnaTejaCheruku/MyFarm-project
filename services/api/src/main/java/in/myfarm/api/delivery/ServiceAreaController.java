package in.myfarm.api.delivery;

import java.util.List;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.GetMapping;
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

	ServiceAreaController(ServiceAreaQueryService serviceAreaQueryService) {
		this.serviceAreaQueryService = serviceAreaQueryService;
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
}
