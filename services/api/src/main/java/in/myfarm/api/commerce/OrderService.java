package in.myfarm.api.commerce;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.myfarm.api.catalog.VariantLookup;
import in.myfarm.api.catalog.VariantSnapshot;
import in.myfarm.api.delivery.DeliveryAvailability;
import in.myfarm.api.payment.PaymentGateway;

@Service
class OrderService {

	private final OrderRepository orderRepository;
	private final DeliveryAvailability deliveryAvailability;
	private final VariantLookup variantLookup;
	private final OrderEventPublisher eventPublisher;
	private final PaymentGateway paymentGateway;

	OrderService(
			OrderRepository orderRepository,
			DeliveryAvailability deliveryAvailability,
			VariantLookup variantLookup,
			OrderEventPublisher eventPublisher,
			PaymentGateway paymentGateway) {
		this.orderRepository = orderRepository;
		this.deliveryAvailability = deliveryAvailability;
		this.variantLookup = variantLookup;
		this.eventPublisher = eventPublisher;
		this.paymentGateway = paymentGateway;
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

		// COD is confirmed the instant it's placed (see OrderEntity's
		// constructor) -- nothing to create a gateway order for. Every
		// other payment method needs the customer to actually pay
		// before the order is confirmed, so a gateway order is created
		// here and the order stays PENDING_PAYMENT until
		// OrderPayments.markPaid (called by the payment module once the
		// gateway confirms) flips it to CONFIRMED.
		PaymentGateway.GatewayOrder gatewayOrder = null;
		if (order.paymentMethod() == PaymentMethod.ONLINE_UPI) {
			gatewayOrder = paymentGateway.createOrder(
					order.orderNumber(), order.subtotalInr());
			order.recordGatewayOrder(gatewayOrder.gatewayOrderId());
		}

		// Published inside the same transaction as the save, before
		// commit -- fine for now (RabbitMQ is a separate connection, not
		// tied to the JDBC transaction), but a real outbox pattern
		// belongs here once dual-write consistency actually matters
		// (observability/reliability phase).
		eventPublisher.publishOrderPlaced(order);

		return toResponse(order, gatewayOrder);
	}

	@Transactional(readOnly = true)
	OrderResponses.Order order(String orderNumber) {
		OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> new OrderNotFoundException(orderNumber));
		// No GatewayOrder to attach here -- that's only returned from
		// the initial placeOrder response, when the storefront actually
		// needs it to continue a payment. Looking an order up later
		// doesn't need Payment.amountPaise/currency again.
		return toResponse(order, null);
	}

	private String generateOrderNumber() {
		return "MF-" + UUID.randomUUID().toString()
				.substring(0, 8).toUpperCase(Locale.ROOT);
	}

	private OrderResponses.Order toResponse(
			OrderEntity order, PaymentGateway.GatewayOrder gatewayOrder) {
		List<OrderResponses.OrderItem> items = order.items().stream()
				.map(item -> new OrderResponses.OrderItem(
						item.sku(),
						new OrderResponses.LocalizedText(
								item.nameEn(), item.nameTe()),
						item.quantity(),
						item.unitPriceInr(),
						item.lineTotalInr()))
				.toList();
		OrderResponses.Payment payment = gatewayOrder == null
				? null
				: new OrderResponses.Payment(
						gatewayOrder.gatewayOrderId(),
						gatewayOrder.amountPaise(),
						gatewayOrder.currency());
		return new OrderResponses.Order(
				order.orderNumber(),
				order.serviceAreaCode(),
				order.deliveryWindowCode(),
				order.customerSubjectId(),
				order.status().name(),
				order.paymentMethod().name(),
				order.subtotalInr(),
				payment,
				items);
	}
}
