package in.myfarm.api.delivery;

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
@Table(name = "delivery_service_area_pincode")
class ServiceAreaPincodeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "service_area_id", nullable = false)
	private ServiceAreaEntity serviceArea;

	@Column(nullable = false, length = 6)
	private String pincode;

	@Column(nullable = false)
	private boolean active;

	@Version
	@Column(nullable = false)
	private long version;

	protected ServiceAreaPincodeEntity() {
	}
}
