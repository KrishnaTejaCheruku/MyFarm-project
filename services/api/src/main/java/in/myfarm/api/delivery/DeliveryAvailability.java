package in.myfarm.api.delivery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Small read-only port for other modules (commerce, when placing an
 * order) to check delivery eligibility without depending on this
 * package's package-private entities/repositories directly.
 */
@Component
@Transactional(readOnly = true)
public class DeliveryAvailability {

	private final ServiceAreaRepository serviceAreaRepository;
	private final DeliveryWindowRepository deliveryWindowRepository;

	DeliveryAvailability(
			ServiceAreaRepository serviceAreaRepository,
			DeliveryWindowRepository deliveryWindowRepository) {
		this.serviceAreaRepository = serviceAreaRepository;
		this.deliveryWindowRepository = deliveryWindowRepository;
	}

	public boolean isServiceAreaActive(String areaCode) {
		return serviceAreaRepository.findByCodeAndActiveTrue(areaCode)
				.isPresent();
	}

	public boolean isDeliveryWindowActive(String areaCode, String windowCode) {
		return serviceAreaRepository.findByCodeAndActiveTrue(areaCode)
				.flatMap(area -> deliveryWindowRepository
						.findByServiceArea_IdAndCodeAndActiveTrue(
								area.id(), windowCode))
				.isPresent();
	}
}
