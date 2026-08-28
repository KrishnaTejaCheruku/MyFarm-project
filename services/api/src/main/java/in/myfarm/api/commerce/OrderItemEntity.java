package in.myfarm.api.commerce;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A snapshot of what a variant was called and cost at the moment the
 * order was placed -- deliberately not a live reference to the catalog,
 * since catalog prices/names change over time and a historical order
 * shouldn't. Write-once: nothing here is ever updated after creation.
 */
@Entity
@Table(name = "commerce_order_item")
class OrderItemEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false, updatable = false)
	private OrderEntity order;

	@Column(name = "variant_id", nullable = false, updatable = false)
	private long variantId;

	@Column(nullable = false, length = 80, updatable = false)
	private String sku;

	@Column(name = "name_en", nullable = false, length = 200, updatable = false)
	private String nameEn;

	@Column(name = "name_te", length = 200, updatable = false)
	private String nameTe;

	@Column(nullable = false, updatable = false)
	private int quantity;

	@Column(name = "unit_price_inr", nullable = false, updatable = false)
	private long unitPriceInr;

	@Column(name = "line_total_inr", nullable = false, updatable = false)
	private long lineTotalInr;

	protected OrderItemEntity() {
	}

	OrderItemEntity(
			OrderEntity order,
			long variantId,
			String sku,
			String nameEn,
			String nameTe,
			int quantity,
			long unitPriceInr) {
		this.order = order;
		this.variantId = variantId;
		this.sku = sku;
		this.nameEn = nameEn;
		this.nameTe = nameTe;
		this.quantity = quantity;
		this.unitPriceInr = unitPriceInr;
		this.lineTotalInr = unitPriceInr * quantity;
	}

	String sku() {
		return sku;
	}

	String nameEn() {
		return nameEn;
	}

	String nameTe() {
		return nameTe;
	}

	int quantity() {
		return quantity;
	}

	long unitPriceInr() {
		return unitPriceInr;
	}

	long lineTotalInr() {
		return lineTotalInr;
	}
}
