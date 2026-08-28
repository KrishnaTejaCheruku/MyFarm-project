package in.myfarm.api.delivery;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ServiceAreaRepository extends JpaRepository<ServiceAreaEntity, Long> {

	List<ServiceAreaEntity> findByActiveTrueOrderByNameEnAscIdAsc();

	Optional<ServiceAreaEntity> findByCodeAndActiveTrue(String code);
}
