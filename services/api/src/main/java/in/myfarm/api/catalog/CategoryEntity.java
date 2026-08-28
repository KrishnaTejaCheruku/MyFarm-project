package in.myfarm.api.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "catalog_category")
class CategoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64, updatable = false)
	private String code;

	@Column(nullable = false, unique = true, length = 120)
	private String slug;

	@Column(name = "name_en", nullable = false, length = 160)
	private String nameEn;

	@Column(name = "name_te", length = 160)
	private String nameTe;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(nullable = false)
	private boolean active;

	@Version
	@Column(nullable = false)
	private long version;

	protected CategoryEntity() {
	}

	Long id() {
		return id;
	}

	String code() {
		return code;
	}

	String slug() {
		return slug;
	}

	String nameEn() {
		return nameEn;
	}

	String nameTe() {
		return nameTe;
	}
}
