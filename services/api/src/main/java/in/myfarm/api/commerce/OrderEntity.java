package in.myfarm.api.commerce;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "commerce_order")
class OrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_number", nullable = false, unique = true, length = 32, updatable = false)
	private String orderNumber;

	// Snapshots, not FKs to the delivery module's entities -- commerce
	// doesn't need to navigate a live ServiceArea/Window object graph,
	// and an order shouldn't change meaning if the area/window it named
	// is edited or deactivated later.
	@Column(name = "service_area_code", nullable = false, length = 64, updatable = false)
	private String serviceAreaCode;

	@Column(name = "delivery_window_code", nullable = false, length = 64, updatable = false)
	private String deliveryWindowCode;

	@Column(name = "customer_name", nullable = false, length = 120, updatable = false)
	private String customerName;

	@Column(name = "customer_phone", nullable = false, length = 10, updatable = false)
	private String customerPhone;

	// The Keycloak subject (JWT "sub" claim) that placed this order --
	// identity/OTP (phase 2) requires an authenticated customer, so
	// this is always populated, unlike customerName/customerPhone
	// which remain free-text checkout fields.
	@Column(name = "customer_subject_id", nullable = false, length = 36, updatable = false)
	private String customerSubjectId;

	@Column(name = "delivery_address_line1", nullable = false, length = 200, updatable = false)
	private String deliveryAddressLine1;

	@Column(name = "delivery_address_line2", length = 200, updatable = false)
	private String deliveryAddressLine2;

	@Column(name = "delivery_pincode", nullable = false, length = 6, updatable = false)
	private String deliveryPincode;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false, length = 16, updatable = false)
	private PaymentMethod paymentMethod;

	// Populated only for gateway-backed payment methods (ONLINE_UPI
	// today). See the payment package -- gatewayOrderId is a single
	// value because a gateway order accepts multiple payment attempts
	// (retries) against the same id until one succeeds.
	@Column(name = "gateway_order_id", length = 64)
	private String gatewayOrderId;

	@Column(name = "gateway_payment_id", length = 64)
	private String gatewayPaymentId;

	@Column(name = "paid_at")
	private Instant paidAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Column(name = "subtotal_inr", nullable = false)
	private long subtotalInr;

	@Version
	@Column(nullable = false)
	private long version;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<OrderItemEntity> items = new ArrayList<>();

	protected OrderEntity() {
	}

	OrderEntity(
			String orderNumber,
			String serviceAreaCode,
			String deliveryWindowCode,
			String customerName,
			String customerPhone,
			String customerSubjectId,
			String deliveryAddressLine1,
			String deliveryAddressLine2,
			String deliveryPincode,
			PaymentMethod paymentMethod) {
		this.orderNumber = orderNumber;
		this.serviceAreaCode = serviceAreaCode;
		this.deliveryWindowCode = deliveryWindowCode;
		this.customerName = customerName;
		this.customerPhone = customerPhone;
		this.customerSubjectId = customerSubjectId;
		this.deliveryAddressLine1 = deliveryAddressLine1;
		this.deliveryAddressLine2 = deliveryAddressLine2;
		this.deliveryPincode = deliveryPincode;
		this.paymentMethod = paymentMethod;
		// COD has nothing to wait on; online orders sit pending until a
		// real payment gateway (Razorpay/UPI, a later phase) confirms.
		this.status = paymentMethod == PaymentMethod.COD
				? OrderStatus.CONFIRMED
				: OrderStatus.PENDING_PAYMENT;
		this.subtotalInr = 0L;
	}

	void addItem(
			long variantId,
			String sku,
			String nameEn,
			String nameTe,
			int quantity,
			long unitPriceInr) {
		OrderItemEntity item = new OrderItemEntity(
				this, variantId, sku, nameEn, nameTe, quantity, unitPriceInr);
		items.add(item);
		subtotalInr += item.lineTotalInr();
	}

	void recordGatewayOrder(String gatewayOrderId) {
		this.gatewayOrderId = gatewayOrderId;
	}

	void markPaid(String gatewayPaymentId) {
		this.gatewayPaymentId = gatewayPaymentId;
		this.paidAt = Instant.now();
		this.status = OrderStatus.CONFIRMED;
	}

	void markPaymentFailed() {
		this.status = OrderStatus.PAYMENT_FAILED;
	}

	String orderNumber() {
		return orderNumber;
	}

	String gatewayOrderId() {
		return gatewayOrderId;
	}

	String serviceAreaCode() {
		return serviceAreaCode;
	}

	String deliveryWindowCode() {
		return deliveryWindowCode;
	}

	String customerSubjectId() {
		return customerSubjectId;
	}

	PaymentMethod paymentMethod() {
		return paymentMethod;
	}

	OrderStatus status() {
		return status;
	}

	long subtotalInr() {
		return subtotalInr;
	}

	List<OrderItemEntity> items() {
		return List.copyOf(items);
	}
}
