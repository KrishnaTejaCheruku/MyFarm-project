package in.myfarm.api.catalog;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "catalog_variant")
class VariantEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private ProductEntity product;

	@Column(nullable = false, unique = true, length = 80, updatable = false)
	private String sku;

	@Column(nullable = false, precision = 10, scale = 3)
	private BigDecimal quantity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private CatalogUnit unit;

	@Column(name = "price_minor", nullable = false)
	private long priceMinor;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "price_tax_inclusive", nullable = false)
	private boolean priceTaxInclusive;

	@Column(name = "gst_basis_points", nullable = false)
	private short gstBasisPoints;

	@Column(name = "subscription_allowed", nullable = false)
	private boolean subscriptionAllowed;

	@Column(name = "image_key", length = 255)
	private String imageKey;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(nullable = false)
	private boolean active;

	@Version
	@Column(nullable = false)
	private long version;

	protected VariantEntity() {
	}

	ProductEntity product() {
		return product;
	}

	String sku() {
		return sku;
	}

	BigDecimal quantity() {
		return quantity;
	}

	CatalogUnit unit() {
		return unit;
	}

	long priceMinor() {
		return priceMinor;
	}

	String currency() {
		return currency;
	}

	boolean priceTaxInclusive() {
		return priceTaxInclusive;
	}

	int gstBasisPoints() {
		return gstBasisPoints;
	}

	boolean subscriptionAllowed() {
		return subscriptionAllowed;
	}

	String imageKey() {
		return imageKey;
	}
}
