package in.myfarm.api.delivery;

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
@Table(name = "delivery_subscription_plan")
class SubscriptionPlanEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "service_area_id", nullable = false)
	private ServiceAreaEntity serviceArea;

	@Column(nullable = false, length = 64, updatable = false)
	private String code;

	@Column(name = "name_en", nullable = false, length = 160)
	private String nameEn;

	@Column(name = "name_te", length = 160)
	private String nameTe;

	@Enumerated(EnumType.STRING)
	@Column(name = "billing_period", nullable = false, length = 16)
	private SubscriptionBillingPeriod billingPeriod;

	@Column(name = "duration_months", nullable = false)
	private int durationMonths;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(nullable = false)
	private boolean active;

	@Version
	@Column(nullable = false)
	private long version;

	protected SubscriptionPlanEntity() {
	}

	String code() {
		return code;
	}

	String nameEn() {
		return nameEn;
	}

	String nameTe() {
		return nameTe;
	}

	SubscriptionBillingPeriod billingPeriod() {
		return billingPeriod;
	}

	int durationMonths() {
		return durationMonths;
	}
}
