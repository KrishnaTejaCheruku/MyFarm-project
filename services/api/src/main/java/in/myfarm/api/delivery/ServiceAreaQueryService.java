package in.myfarm.api.delivery;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ServiceAreaQueryService {

	private final ServiceAreaRepository serviceAreaRepository;
	private final ServiceAreaPincodeRepository pincodeRepository;

	ServiceAreaQueryService(
			ServiceAreaRepository serviceAreaRepository,
			ServiceAreaPincodeRepository pincodeRepository) {
		this.serviceAreaRepository = serviceAreaRepository;
		this.pincodeRepository = pincodeRepository;
	}

	List<ServiceAreaResponses.ServiceArea> serviceAreas() {
		return serviceAreaRepository.findByActiveTrueOrderByNameEnAscIdAsc()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	ServiceAreaResponses.Eligibility eligibility(
			String areaCode, String pincode) {
		Optional<ServiceAreaEntity> area = serviceAreaRepository
				.findByCodeAndActiveTrue(areaCode);
		if (area.isEmpty()) {
			return new ServiceAreaResponses.Eligibility(
					areaCode, pincode, false, null);
		}

		boolean serviceable = pincodeRepository.countActiveMappings(
				areaCode, pincode) > 0;
		return new ServiceAreaResponses.Eligibility(
				areaCode, pincode, serviceable, toResponse(area.get()));
	}

	private ServiceAreaResponses.ServiceArea toResponse(ServiceAreaEntity area) {
		return new ServiceAreaResponses.ServiceArea(
				area.code(),
				new ServiceAreaResponses.LocalizedText(
						area.nameEn(), area.nameTe()),
				area.city(),
				area.state(),
				area.timezone(),
				area.subscriptionRequired());
	}
}
