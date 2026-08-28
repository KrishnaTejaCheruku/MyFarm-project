package in.myfarm.api.commerce;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.myfarm.api.catalog.VariantLookup;
import in.myfarm.api.catalog.VariantSnapshot;
import in.myfarm.api.delivery.DeliveryAvailability;

@Service
class OrderService {

	private final OrderRepository orderRepository;
	private final DeliveryAvailability deliveryAvailability;
	private final VariantLookup variantLookup;
	private final OrderEventPublisher eventPublisher;

	OrderService(
			OrderRepository orderRepository,
			DeliveryAvailability deliveryAvailability,
			VariantLookup variantLookup,
			OrderEventPublisher eventPublisher) {
		this.orderRepository = orderRepository;
		this.deliveryAvailability = deliveryAvailability;
		this.variantLookup = variantLookup;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	OrderResponses.Order placeOrder(
			OrderRequests.PlaceOrder request, String customerSubjectId) {
		if (!deliveryAvailability.isServiceAreaActive(
				request.serviceAreaCode())) {
			throw new InvalidOrderException(
					"Service area '%s' is not active"
							.formatted(request.serviceAreaCode()));
		}
		if (!deliveryAvailability.isDeliveryWindowActive(
				request.serviceAreaCode(), request.deliveryWindowCode())) {
			throw new InvalidOrderException(
					"Delivery window '%s' is not active for service area '%s'"
							.formatted(
									request.deliveryWindowCode(),
									request.serviceAreaCode()));
		}

		OrderEntity order = new OrderEntity(
				generateOrderNumber(),
				request.serviceAreaCode(),
				request.deliveryWindowCode(),
				request.customerName(),
				request.customerPhone(),
				customerSubjectId,
				request.deliveryAddressLine1(),
				request.deliveryAddressLine2(),
				request.deliveryPincode(),
				request.paymentMethod());

		for (OrderRequests.Item item : request.items()) {
			VariantSnapshot variant = variantLookup.findActive(item.sku())
					.orElseThrow(() -> new InvalidOrderException(
							"Variant '%s' is not available"
									.formatted(item.sku())));
			order.addItem(
					variant.id(),
					variant.sku(),
					variant.nameEn(),
					variant.nameTe(),
					item.quantity(),
					variant.priceInr());
		}

		order = orderRepository.save(order);

		// Published inside the same transaction as the save, before
		// commit -- fine for now (RabbitMQ is a separate connection, not
		// tied to the JDBC transaction), but a real outbox pattern
		// belongs here once dual-write consistency actually matters
		// (observability/reliability phase).
		eventPublisher.publishOrderPlaced(order);

		return toResponse(order);
	}

	@Transactional(readOnly = true)
	OrderResponses.Order order(String orderNumber) {
		OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> new OrderNotFoundException(orderNumber));
		return toResponse(order);
	}

	private String generateOrderNumber() {
		return "MF-" + UUID.randomUUID().toString()
				.substring(0, 8).toUpperCase(Locale.ROOT);
	}

	private OrderResponses.Order toResponse(OrderEntity order) {
		List<OrderResponses.OrderItem> items = order.items().stream()
				.map(item -> new OrderResponses.OrderItem(
						item.sku(),
						new OrderResponses.LocalizedText(
								item.nameEn(), item.nameTe()),
						item.quantity(),
						item.unitPriceInr(),
						item.lineTotalInr()))
				.toList();
		return new OrderResponses.Order(
				order.orderNumber(),
				order.serviceAreaCode(),
				order.deliveryWindowCode(),
				order.customerSubjectId(),
				order.status().name(),
				order.paymentMethod().name(),
				order.subtotalInr(),
				items);
	}
}
