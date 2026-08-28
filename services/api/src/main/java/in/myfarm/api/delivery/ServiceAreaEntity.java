package in.myfarm.api.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "delivery_service_area")
class ServiceAreaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64, updatable = false)
	private String code;

	@Column(name = "name_en", nullable = false, length = 160)
	private String nameEn;

	@Column(name = "name_te", length = 160)
	private String nameTe;

	@Column(nullable = false, length = 120)
	private String city;

	@Column(nullable = false, length = 120)
	private String state;

	@Column(nullable = false, length = 64)
	private String timezone;

	@Column(name = "subscription_required", nullable = false)
	private boolean subscriptionRequired;

	@Column(nullable = false)
	private boolean active;

	@Version
	@Column(nullable = false)
	private long version;

	protected ServiceAreaEntity() {
	}

	Long id() {
		return id;
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

	String city() {
		return city;
	}

	String state() {
		return state;
	}

	String timezone() {
		return timezone;
	}

	boolean subscriptionRequired() {
		return subscriptionRequired;
	}
}
