package in.myfarm.api.delivery;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "delivery_window")
class DeliveryWindowEntity {

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

	@Column(name = "starts_at", nullable = false)
	private LocalTime startsAt;

	@Column(name = "ends_at", nullable = false)
	private LocalTime endsAt;

	@Column(name = "cutoff_minutes_before", nullable = false)
	private int cutoffMinutesBefore;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(nullable = false)
	private boolean active;

	@Version
	@Column(nullable = false)
	private long version;

	protected DeliveryWindowEntity() {
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

	LocalTime startsAt() {
		return startsAt;
	}

	LocalTime endsAt() {
		return endsAt;
	}

	int cutoffMinutesBefore() {
		return cutoffMinutesBefore;
	}
}
