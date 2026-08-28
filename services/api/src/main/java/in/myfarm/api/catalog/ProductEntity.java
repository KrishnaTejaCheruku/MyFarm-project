package in.myfarm.api.catalog;

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
@Table(name = "catalog_product")
class ProductEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private CategoryEntity category;

	@Column(nullable = false, unique = true, length = 64, updatable = false)
	private String code;

	@Column(nullable = false, unique = true, length = 160)
	private String slug;

	@Column(name = "name_en", nullable = false, length = 200)
	private String nameEn;

	@Column(name = "name_te", length = 200)
	private String nameTe;

	@Column(name = "description_en", length = 2000)
	private String descriptionEn;

	@Column(name = "description_te", length = 2000)
	private String descriptionTe;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(nullable = false)
	private boolean active;

	@Version
	@Column(nullable = false)
	private long version;

	protected ProductEntity() {
	}

	Long id() {
		return id;
	}

	CategoryEntity category() {
		return category;
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

	String descriptionEn() {
		return descriptionEn;
	}

	String descriptionTe() {
		return descriptionTe;
	}
}
