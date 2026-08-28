package in.myfarm.api.commerce;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<OrderEntity, Long> {

	Optional<OrderEntity> findByOrderNumber(String orderNumber);
}
