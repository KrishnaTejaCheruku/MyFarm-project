package in.myfarm.api.delivery;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface DeliveryWindowRepository
		extends JpaRepository<DeliveryWindowEntity, Long> {

	List<DeliveryWindowEntity>
			findByServiceArea_IdAndActiveTrueOrderBySortOrderAscIdAsc(
					Long serviceAreaId);

	Optional<DeliveryWindowEntity>
			findByServiceArea_IdAndCodeAndActiveTrue(
					Long serviceAreaId, String code);
}
