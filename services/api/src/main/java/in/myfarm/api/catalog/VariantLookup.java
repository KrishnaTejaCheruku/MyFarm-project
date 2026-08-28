package in.myfarm.api.catalog;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VariantLookup {

	private final VariantRepository variantRepository;

	VariantLookup(VariantRepository variantRepository) {
		this.variantRepository = variantRepository;
	}

	@Transactional(readOnly = true)
	public Optional<VariantSnapshot> findActive(String sku) {
		return variantRepository.findBySkuAndActiveTrue(sku)
				.map(variant -> new VariantSnapshot(
						variant.id(),
						variant.sku(),
						variant.product().nameEn(),
						variant.product().nameTe(),
						variant.priceInr()));
	}
}
