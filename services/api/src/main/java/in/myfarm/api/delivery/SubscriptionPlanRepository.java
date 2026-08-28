package in.myfarm.api.delivery;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface SubscriptionPlanRepository
		extends JpaRepository<SubscriptionPlanEntity, Long> {

	List<SubscriptionPlanEntity>
			findByServiceArea_IdAndActiveTrueOrderBySortOrderAscIdAsc(
					Long serviceAreaId);

	Optional<SubscriptionPlanEntity>
			findByServiceArea_IdAndCodeAndActiveTrue(
					Long serviceAreaId, String code);
}
