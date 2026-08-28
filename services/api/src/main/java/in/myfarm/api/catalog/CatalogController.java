package in.myfarm.api.catalog;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

	private static final String CODE_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";

	private final CatalogQueryService catalogQueryService;

	CatalogController(CatalogQueryService catalogQueryService) {
		this.catalogQueryService = catalogQueryService;
	}

	@GetMapping("/categories")
	public List<CatalogResponses.Category> categories() {
		return catalogQueryService.categories();
	}

	@GetMapping("/products")
	public CatalogResponses.Page<CatalogResponses.Product> products(
			@RequestParam(required = false)
			@Size(max = 64)
			@Pattern(regexp = CODE_PATTERN) String category,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
		return catalogQueryService.products(category, page, size);
	}

	@GetMapping("/products/{slug}")
	public CatalogResponses.Product product(
			@PathVariable
			@Size(max = 160)
			@Pattern(regexp = CODE_PATTERN) String slug) {
		return catalogQueryService.product(slug);
	}
}
