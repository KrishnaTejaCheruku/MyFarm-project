package in.myfarm.api.catalog;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
class CatalogQueryService {

	private static final Sort PRODUCT_ORDER = Sort.by(
			Sort.Order.asc("sortOrder"), Sort.Order.asc("id"));

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final VariantRepository variantRepository;

	CatalogQueryService(
			CategoryRepository categoryRepository,
			ProductRepository productRepository,
			VariantRepository variantRepository) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
		this.variantRepository = variantRepository;
	}

	List<CatalogResponses.Category> categories() {
		return categoryRepository.findByActiveTrueOrderBySortOrderAscIdAsc()
				.stream()
				.map(this::toCategory)
				.toList();
	}

	CatalogResponses.Page<CatalogResponses.Product> products(
			String categoryCode, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, PRODUCT_ORDER);
		org.springframework.data.domain.Page<ProductEntity> products =
				findProducts(categoryCode, pageable);

		Map<Long, List<VariantEntity>> variants = variantsFor(
				products.getContent().stream().map(ProductEntity::id).toList());

		List<CatalogResponses.Product> items = products.getContent()
				.stream()
				.map(product -> toProduct(
						product, variants.getOrDefault(product.id(), List.of())))
				.toList();

		return new CatalogResponses.Page<>(
				items,
				products.getNumber(),
				products.getSize(),
				products.getTotalElements(),
				products.getTotalPages());
	}

	CatalogResponses.Product product(String slug) {
		ProductEntity product = productRepository.findBySlugAndActiveTrue(slug)
				.orElseThrow(() -> new CatalogProductNotFoundException(slug));
		List<VariantEntity> variants = variantRepository
				.findActiveByProductIds(List.of(product.id()));
		return toProduct(product, variants);
	}

	private org.springframework.data.domain.Page<ProductEntity> findProducts(
			String categoryCode, Pageable pageable) {
		if (!StringUtils.hasText(categoryCode)) {
			return productRepository.findByActiveTrue(pageable);
		}
		return productRepository.findByActiveTrueAndCategory_CodeIgnoreCase(
				categoryCode.trim(), pageable);
	}

	private Map<Long, List<VariantEntity>> variantsFor(Collection<Long> productIds) {
		if (productIds.isEmpty()) {
			return Map.of();
		}
		return variantRepository
				.findActiveByProductIds(productIds)
				.stream()
				.collect(groupingBy(
						variant -> variant.product().id(),
						LinkedHashMap::new,
						toList()));
	}

	private CatalogResponses.Category toCategory(CategoryEntity category) {
		return new CatalogResponses.Category(
				category.code(),
				category.slug(),
				new CatalogResponses.LocalizedText(
						category.nameEn(), category.nameTe()));
	}

	private CatalogResponses.Product toProduct(
			ProductEntity product, List<VariantEntity> variants) {
		return new CatalogResponses.Product(
				product.code(),
				product.slug(),
				product.category().code(),
				new CatalogResponses.LocalizedText(
						product.nameEn(), product.nameTe()),
				new CatalogResponses.LocalizedText(
						product.descriptionEn(), product.descriptionTe()),
				variants.stream().map(this::toVariant).toList());
	}

	private CatalogResponses.Variant toVariant(VariantEntity variant) {
		return new CatalogResponses.Variant(
				variant.sku(),
				variant.quantity(),
				variant.unit().name().toLowerCase(Locale.ROOT),
				new CatalogResponses.Money(
						variant.currency(),
						variant.priceMinor(),
						variant.priceTaxInclusive()),
				variant.gstBasisPoints(),
				variant.subscriptionAllowed(),
				variant.imageKey());
	}
}
